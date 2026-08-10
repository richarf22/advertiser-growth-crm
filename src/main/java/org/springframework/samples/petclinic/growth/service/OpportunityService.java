package org.springframework.samples.petclinic.growth.service;

import java.util.Optional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.OpportunityRequest;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.TransitionRequest;
import org.springframework.samples.petclinic.growth.model.AdvertiserAccount;
import org.springframework.samples.petclinic.growth.model.AuditEvent;
import org.springframework.samples.petclinic.growth.model.Opportunity;
import org.springframework.samples.petclinic.growth.model.OpportunityStage;
import org.springframework.samples.petclinic.growth.model.StageTransition;
import org.springframework.samples.petclinic.growth.repository.AdvertiserAccountRepository;
import org.springframework.samples.petclinic.growth.repository.AuditEventRepository;
import org.springframework.samples.petclinic.growth.repository.OpportunityRepository;
import org.springframework.samples.petclinic.growth.repository.StageTransitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityService {
	private final AdvertiserAccountRepository accounts;
	private final OpportunityRepository opportunities;
	private final StageTransitionRepository transitions;
	private final AuditEventRepository events;
	private final OpportunityStagePolicy stagePolicy;
	private final Counter transitionCounter;

	public OpportunityService(AdvertiserAccountRepository accounts, OpportunityRepository opportunities,
		StageTransitionRepository transitions, AuditEventRepository events, OpportunityStagePolicy stagePolicy,
		MeterRegistry registry) {
		this.accounts = accounts;
		this.opportunities = opportunities;
		this.transitions = transitions;
		this.events = events;
		this.stagePolicy = stagePolicy;
		this.transitionCounter = Counter.builder("growth.opportunity.transitions")
			.description("Committed opportunity stage transitions").register(registry);
	}

	@Transactional
	public Opportunity create(long accountId, OpportunityRequest request) {
		AdvertiserAccount account = accounts.findById(accountId)
			.orElseThrow(() -> new ResourceNotFoundException("Advertiser account not found: " + accountId));
		Opportunity opportunity = new Opportunity();
		opportunity.setAccount(account);
		opportunity.setName(request.name().trim());
		opportunity.setValueCents(request.valueCents());
		opportunity.setCurrency(request.currency());
		opportunity.setStage(OpportunityStage.PROSPECTING);
		return opportunities.save(opportunity);
	}

	@Transactional(readOnly = true)
	public Opportunity get(long id) {
		return opportunities.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
	}

	@Transactional(readOnly = true)
	public Page<Opportunity> search(Long accountId, OpportunityStage stage, Pageable pageable) {
		Specification<Opportunity> specification = (root, query, builder) -> builder.conjunction();
		if (accountId != null) {
			specification = specification.and((root, query, builder) -> builder.equal(root.get("account").get("id"), accountId));
		}
		if (stage != null) {
			specification = specification.and((root, query, builder) -> builder.equal(root.get("stage"), stage));
		}
		return opportunities.findAll(specification, pageable);
	}

	@Transactional
	public StageTransition transition(long opportunityId, String requestId, TransitionRequest request) {
		Optional<StageTransition> duplicate = transitions.findByRequestId(requestId);
		if (duplicate.isPresent()) {
			StageTransition existing = duplicate.get();
			if (!existing.getOpportunity().getId().equals(opportunityId) || existing.getToStage() != request.toStage()) {
				throw new BusinessConflictException("Idempotency key was already used for a different transition");
			}
			return existing;
		}

		Opportunity opportunity = opportunities.findById(opportunityId)
			.orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + opportunityId));
		OpportunityStage from = opportunity.getStage();
		stagePolicy.requireAllowed(from, request.toStage());
		opportunity.setStage(request.toStage());
		opportunities.save(opportunity);

		StageTransition transition = new StageTransition();
		transition.setOpportunity(opportunity);
		transition.setFromStage(from);
		transition.setToStage(request.toStage());
		transition.setActor(request.actor().trim());
		transition.setRequestId(requestId);
		StageTransition saved = transitions.save(transition);

		AuditEvent event = new AuditEvent();
		event.setEventType("opportunity.stage.changed");
		event.setAggregateType("Opportunity");
		event.setAggregateId(String.valueOf(opportunityId));
		event.setPayload("{\"opportunityId\":" + opportunityId + ",\"fromStage\":\"" + from
			+ "\",\"toStage\":\"" + request.toStage() + "\",\"requestId\":\"" + requestId + "\"}");
		events.save(event);
		transitionCounter.increment();
		return saved;
	}
}
