package org.springframework.samples.petclinic.growth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.profiles.active=h2,spring-data-jpa",
	"spring.datasource.url=jdbc:h2:mem:growthapi;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	"petclinic.security.enable=false",
	"growth.outbox.enabled=false"
})
@AutoConfigureMockMvc
class GrowthApiIntegrationTests {
	@Autowired
	private MockMvc mvc;

	@Test
	void executesAccountOpportunityAndIdempotentTransitionFlow() throws Exception {
		String accountBody = """
			{"externalId":"adv-api-test","name":"Demo Advertiser","region":"US","industry":"Retail"}
			""";
		String accountJson = mvc.perform(post("/api/growth/accounts")
				.contentType(MediaType.APPLICATION_JSON).content(accountBody))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long accountId = Long.parseLong(accountJson.replaceAll(".*\"id\":(\\d+).*", "$1"));

		String opportunityBody = """
			{"name":"API Growth Campaign","valueCents":1200000,"currency":"USD"}
			""";
		String opportunityJson = mvc.perform(post("/api/growth/accounts/{id}/opportunities", accountId)
				.contentType(MediaType.APPLICATION_JSON).content(opportunityBody))
			.andExpect(status().isCreated()).andExpect(jsonPath("$.stage").value("PROSPECTING"))
			.andReturn().getResponse().getContentAsString();
		long opportunityId = Long.parseLong(opportunityJson.replaceAll(".*\"id\":(\\d+).*", "$1"));

		String transitionBody = """
			{"toStage":"QUALIFIED","actor":"integration-test"}
			""";
		mvc.perform(post("/api/growth/opportunities/{id}/transitions", opportunityId)
				.header("Idempotency-Key", "integration-transition-1")
				.contentType(MediaType.APPLICATION_JSON).content(transitionBody))
			.andExpect(status().isOk()).andExpect(jsonPath("$.fromStage").value("PROSPECTING"))
			.andExpect(jsonPath("$.toStage").value("QUALIFIED"));

		mvc.perform(post("/api/growth/opportunities/{id}/transitions", opportunityId)
				.header("Idempotency-Key", "integration-transition-1")
				.contentType(MediaType.APPLICATION_JSON).content(transitionBody))
			.andExpect(status().isOk()).andExpect(jsonPath("$.requestId").value("integration-transition-1"));

		mvc.perform(get("/api/growth/opportunities").param("stage", "QUALIFIED"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
		mvc.perform(get("/api/growth/audit-events"))
			.andExpect(status().isOk()).andExpect(jsonPath("$[0].eventType").value("opportunity.stage.changed"));
	}

	@Test
	void rejectsIllegalStageSkipWithProblemDetails() throws Exception {
		mvc.perform(post("/api/growth/opportunities/1/transitions")
				.header("Idempotency-Key", "illegal-transition-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"toStage\":\"WON\",\"actor\":\"integration-test\"}"))
			.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void mapsDuplicateAdvertiserIdentityToConflictProblemDetails() throws Exception {
		String duplicate = """
			{"externalId":"adv-demo-001","name":"Duplicate","region":"US","industry":"Retail"}
			""";
		mvc.perform(post("/api/growth/accounts").contentType(MediaType.APPLICATION_JSON).content(duplicate))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409));
	}
}
