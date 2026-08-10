package org.springframework.samples.petclinic.growth.api;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.samples.petclinic.growth.model.AdvertiserAccount;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.samples.petclinic.growth.model.Opportunity;
import org.springframework.samples.petclinic.growth.model.OpportunityStage;
import org.springframework.samples.petclinic.growth.model.StageTransition;

public final class GrowthDtos {
	private GrowthDtos() { }

	public record AccountRequest(
		@NotBlank @Size(max = 64) String externalId,
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Pattern(regexp = "[A-Z]{2,8}") String region,
		@NotBlank @Size(max = 80) String industry) { }

	public record AccountResponse(Long id, String externalId, String name, String region,
		String industry, Instant createdAt, Instant updatedAt) {
		public static AccountResponse from(AdvertiserAccount account) {
			return new AccountResponse(account.getId(), account.getExternalId(), account.getName(),
				account.getRegion(), account.getIndustry(), account.getCreatedAt(), account.getUpdatedAt());
		}
	}

	public record OpportunityRequest(
		@NotBlank @Size(max = 140) String name,
		@NotNull @PositiveOrZero Long valueCents,
		@NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) { }

	public record OpportunityResponse(Long id, Long accountId, String name, OpportunityStage stage,
		Long valueCents, String currency, Long version, Instant createdAt, Instant updatedAt) {
		public static OpportunityResponse from(Opportunity opportunity) {
			return new OpportunityResponse(opportunity.getId(), opportunity.getAccount().getId(),
				opportunity.getName(), opportunity.getStage(), opportunity.getValueCents(),
				opportunity.getCurrency(), opportunity.getVersion(), opportunity.getCreatedAt(), opportunity.getUpdatedAt());
		}
	}

	public record TransitionRequest(@NotNull OpportunityStage toStage,
		@NotBlank @Size(max = 80) String actor) { }

	public record TransitionResponse(Long transitionId, Long opportunityId, OpportunityStage fromStage,
		OpportunityStage toStage, String actor, String requestId, Instant occurredAt) {
		public static TransitionResponse from(StageTransition transition) {
			return new TransitionResponse(transition.getId(), transition.getOpportunity().getId(),
				transition.getFromStage(), transition.getToStage(), transition.getActor(),
				transition.getRequestId(), transition.getOccurredAt());
		}
	}

	public record AuditEventResponse(Long id, String eventType, String aggregateType, String aggregateId,
		String payload, AuditEvent.Status status, int attempts, String lastError, Instant createdAt,
		Instant publishedAt) {
		public static AuditEventResponse from(AuditEvent event) {
			return new AuditEventResponse(event.getId(), event.getEventType(), event.getAggregateType(),
				event.getAggregateId(), event.getPayload(), event.getStatus(), event.getAttempts(),
				event.getLastError(), event.getCreatedAt(), event.getPublishedAt());
		}
	}

	public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
}
