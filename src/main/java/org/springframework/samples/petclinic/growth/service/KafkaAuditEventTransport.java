package org.springframework.samples.petclinic.growth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "growth.kafka.enabled", havingValue = "true")
public class KafkaAuditEventTransport implements AuditEventTransport {
	private final KafkaTemplate<String, String> kafka;
	private final String topic;

	public KafkaAuditEventTransport(KafkaTemplate<String, String> kafka,
		@Value("${growth.kafka.topic:growth.audit-events}") String topic) {
		this.kafka = kafka;
		this.topic = topic;
	}

	@Override
	public void publish(AuditEvent event) throws Exception {
		kafka.send(topic, event.getAggregateId(), event.getPayload()).get(5, TimeUnit.SECONDS);
	}
}
