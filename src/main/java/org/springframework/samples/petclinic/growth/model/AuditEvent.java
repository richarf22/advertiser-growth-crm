package org.springframework.samples.petclinic.growth.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
	public enum Status { PENDING, PUBLISHED, FAILED }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "event_type", nullable = false, length = 80)
	private String eventType;
	@Column(name = "aggregate_type", nullable = false, length = 40)
	private String aggregateType;
	@Column(name = "aggregate_id", nullable = false, length = 80)
	private String aggregateId;
	@Column(nullable = false, length = 2000)
	private String payload;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private Status status;
	@Column(nullable = false)
	private int attempts;
	@Column(name = "last_error", length = 500)
	private String lastError;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "published_at")
	private Instant publishedAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		if (status == null) status = Status.PENDING;
	}

	public Long getId() { return id; }
	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }
	public String getAggregateType() { return aggregateType; }
	public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
	public String getAggregateId() { return aggregateId; }
	public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
	public String getPayload() { return payload; }
	public void setPayload(String payload) { this.payload = payload; }
	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = status; }
	public int getAttempts() { return attempts; }
	public void setAttempts(int attempts) { this.attempts = attempts; }
	public String getLastError() { return lastError; }
	public void setLastError(String lastError) { this.lastError = lastError; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getPublishedAt() { return publishedAt; }
	public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}
