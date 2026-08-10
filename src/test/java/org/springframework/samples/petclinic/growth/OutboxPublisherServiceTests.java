package org.springframework.samples.petclinic.growth;

import java.util.List;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.samples.petclinic.growth.repository.AuditEventRepository;
import org.springframework.samples.petclinic.growth.service.AuditEventTransport;
import org.springframework.samples.petclinic.growth.service.OutboxPublisherService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherServiceTests {
	@Test
	void retainsFailedKafkaEventForRetry() throws Exception {
		AuditEventRepository repository = mock(AuditEventRepository.class);
		AuditEventTransport transport = mock(AuditEventTransport.class);
		AuditEvent event = new AuditEvent();
		event.setEventType("opportunity.stage.changed");
		event.setAggregateType("Opportunity");
		event.setAggregateId("42");
		event.setPayload("{}");
		event.setStatus(AuditEvent.Status.PENDING);
		when(repository.findReady(any(), anyInt(), any(Pageable.class))).thenReturn(List.of(event));
		org.mockito.Mockito.doThrow(new IllegalStateException("Kafka unavailable")).when(transport).publish(event);

		new OutboxPublisherService(repository, transport, new SimpleMeterRegistry()).publishBatch();

		assertThat(event.getStatus()).isEqualTo(AuditEvent.Status.FAILED);
		assertThat(event.getAttempts()).isEqualTo(1);
		assertThat(event.getLastError()).contains("Kafka unavailable");
		verify(repository).save(event);
	}
}
