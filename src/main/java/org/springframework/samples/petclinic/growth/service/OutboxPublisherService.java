package org.springframework.samples.petclinic.growth.service;

import java.time.Instant;
import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.samples.petclinic.growth.repository.AuditEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "growth.outbox.enabled", havingValue = "true")
public class OutboxPublisherService {
	private final AuditEventRepository events;
	private final AuditEventTransport transport;
	private final Counter published;
	private final Counter failed;

	public OutboxPublisherService(AuditEventRepository events, AuditEventTransport transport, MeterRegistry registry) {
		this.events = events;
		this.transport = transport;
		this.published = Counter.builder("growth.outbox.published").register(registry);
		this.failed = Counter.builder("growth.outbox.failed").register(registry);
	}

	@Scheduled(fixedDelayString = "${growth.outbox.poll-ms:2000}")
	@Transactional
	public void publishBatch() {
		List<AuditEvent> ready = events.findReady(
			List.of(AuditEvent.Status.PENDING, AuditEvent.Status.FAILED), 5, PageRequest.of(0, 50));
		for (AuditEvent event : ready) {
			try {
				transport.publish(event);
				event.setStatus(AuditEvent.Status.PUBLISHED);
				event.setPublishedAt(Instant.now());
				event.setLastError(null);
				published.increment();
			} catch (Exception exception) {
				event.setStatus(AuditEvent.Status.FAILED);
				event.setAttempts(event.getAttempts() + 1);
				event.setLastError(truncate(exception.getMessage()));
				failed.increment();
			}
			events.save(event);
		}
	}

	private String truncate(String message) {
		if (message == null) return "Unknown transport error";
		return message.length() <= 500 ? message : message.substring(0, 500);
	}
}
