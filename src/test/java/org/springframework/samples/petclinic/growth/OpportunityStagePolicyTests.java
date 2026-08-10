package org.springframework.samples.petclinic.growth;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.growth.model.OpportunityStage;
import org.springframework.samples.petclinic.growth.service.BusinessConflictException;
import org.springframework.samples.petclinic.growth.service.OpportunityStagePolicy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpportunityStagePolicyTests {
	private final OpportunityStagePolicy policy = new OpportunityStagePolicy();

	@Test
	void permitsForwardRevenueLifecycle() {
		assertThatCode(() -> policy.requireAllowed(OpportunityStage.PROSPECTING, OpportunityStage.QUALIFIED))
			.doesNotThrowAnyException();
		assertThatCode(() -> policy.requireAllowed(OpportunityStage.NEGOTIATION, OpportunityStage.WON))
			.doesNotThrowAnyException();
	}

	@Test
	void rejectsSkippedAndTerminalTransitions() {
		assertThatThrownBy(() -> policy.requireAllowed(OpportunityStage.PROSPECTING, OpportunityStage.WON))
			.isInstanceOf(BusinessConflictException.class);
		assertThatThrownBy(() -> policy.requireAllowed(OpportunityStage.WON, OpportunityStage.QUALIFIED))
			.isInstanceOf(BusinessConflictException.class);
	}
}
