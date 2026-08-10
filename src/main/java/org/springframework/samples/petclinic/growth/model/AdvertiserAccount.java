package org.springframework.samples.petclinic.growth.model;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "advertiser_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_advertiser_external_id", columnNames = "external_id"))
public class AdvertiserAccount implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_id", nullable = false, length = 64)
	private String externalId;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 16)
	private String region;

	@Column(nullable = false, length = 80)
	private String industry;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getExternalId() { return externalId; }
	public void setExternalId(String externalId) { this.externalId = externalId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
	public String getIndustry() { return industry; }
	public void setIndustry(String industry) { this.industry = industry; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
