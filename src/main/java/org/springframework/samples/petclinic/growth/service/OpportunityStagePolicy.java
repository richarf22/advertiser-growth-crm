package org.springframework.samples.petclinic.growth.service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.samples.petclinic.growth.model.OpportunityStage;
import org.springframework.stereotype.Component;

@Component
public class OpportunityStagePolicy {
	private static final Map<OpportunityStage, Set<OpportunityStage>> ALLOWED = Map.of(
		OpportunityStage.PROSPECTING, EnumSet.of(OpportunityStage.QUALIFIED, OpportunityStage.LOST),
		OpportunityStage.QUALIFIED, EnumSet.of(OpportunityStage.PROPOSAL, OpportunityStage.LOST),
		OpportunityStage.PROPOSAL, EnumSet.of(OpportunityStage.NEGOTIATION, OpportunityStage.LOST),
		OpportunityStage.NEGOTIATION, EnumSet.of(OpportunityStage.WON, OpportunityStage.LOST),
		OpportunityStage.WON, EnumSet.noneOf(OpportunityStage.class),
		OpportunityStage.LOST, EnumSet.noneOf(OpportunityStage.class));

	public void requireAllowed(OpportunityStage from, OpportunityStage to) {
		if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
			throw new BusinessConflictException("Opportunity stage cannot move from " + from + " to " + to);
		}
	}
}
