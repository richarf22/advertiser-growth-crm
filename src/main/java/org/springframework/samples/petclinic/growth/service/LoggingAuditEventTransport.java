package org.springframework.samples.petclinic.growth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "growth.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingAuditEventTransport implements AuditEventTransport {
	private static final Logger logger = LoggerFactory.getLogger(LoggingAuditEventTransport.class);
	@Override
	public void publish(AuditEvent event) {
		logger.info("Local audit event id={} type={} aggregate={}:{}", event.getId(), event.getEventType(),
			event.getAggregateType(), event.getAggregateId());
	}
}
