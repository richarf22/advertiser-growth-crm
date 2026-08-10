package org.springframework.samples.petclinic.growth.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stage_transitions", uniqueConstraints = @UniqueConstraint(name = "uk_transition_request_id", columnNames = "request_id"))
public class StageTransition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "opportunity_id", nullable = false)
	private Opportunity opportunity;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_stage", nullable = false, length = 24)
	private OpportunityStage fromStage;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_stage", nullable = false, length = 24)
	private OpportunityStage toStage;

	@Column(nullable = false, length = 80)
	private String actor;

	@Column(name = "request_id", nullable = false, length = 80)
	private String requestId;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@PrePersist
	void onCreate() { occurredAt = Instant.now(); }

	public Long getId() { return id; }
	public Opportunity getOpportunity() { return opportunity; }
	public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }
	public OpportunityStage getFromStage() { return fromStage; }
	public void setFromStage(OpportunityStage fromStage) { this.fromStage = fromStage; }
	public OpportunityStage getToStage() { return toStage; }
	public void setToStage(OpportunityStage toStage) { this.toStage = toStage; }
	public String getActor() { return actor; }
	public void setActor(String actor) { this.actor = actor; }
	public String getRequestId() { return requestId; }
	public void setRequestId(String requestId) { this.requestId = requestId; }
	public Instant getOccurredAt() { return occurredAt; }
}
