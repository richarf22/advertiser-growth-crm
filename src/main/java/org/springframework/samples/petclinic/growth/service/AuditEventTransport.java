package org.springframework.samples.petclinic.growth.service;

import org.springframework.samples.petclinic.growth.model.AuditEvent;

public interface AuditEventTransport {
	void publish(AuditEvent event) throws Exception;
}
