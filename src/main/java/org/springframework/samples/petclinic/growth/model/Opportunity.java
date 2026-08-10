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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "opportunities")
public class Opportunity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private AdvertiserAccount account;

	@Column(nullable = false, length = 140)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private OpportunityStage stage;

	@Column(name = "value_cents", nullable = false)
	private Long valueCents;

	@Column(nullable = false, length = 3)
	private String currency;

	@Version
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (stage == null) stage = OpportunityStage.PROSPECTING;
	}

	@PreUpdate
	void onUpdate() { updatedAt = Instant.now(); }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public AdvertiserAccount getAccount() { return account; }
	public void setAccount(AdvertiserAccount account) { this.account = account; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public OpportunityStage getStage() { return stage; }
	public void setStage(OpportunityStage stage) { this.stage = stage; }
	public Long getValueCents() { return valueCents; }
	public void setValueCents(Long valueCents) { this.valueCents = valueCents; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public Long getVersion() { return version; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
