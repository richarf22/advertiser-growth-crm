package org.springframework.samples.petclinic.growth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.profiles.active=h2,spring-data-jpa",
	"spring.datasource.url=jdbc:h2:mem:growthsecurity;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	"petclinic.security.enable=true",
	"growth.outbox.enabled=false"
})
@AutoConfigureMockMvc
class GrowthSecurityTests {
	@Autowired
	private MockMvc mvc;

	@Test
	@WithMockUser(roles = "GROWTH_VIEWER")
	void viewerCanReadButCannotCreate() throws Exception {
		mvc.perform(get("/api/growth/accounts/1")).andExpect(status().isOk());
		mvc.perform(post("/api/growth/accounts").contentType(MediaType.APPLICATION_JSON)
			.content("{\"externalId\":\"blocked\",\"name\":\"Blocked\",\"region\":\"US\",\"industry\":\"Retail\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "GROWTH_MANAGER")
	void managerCanCreate() throws Exception {
		mvc.perform(post("/api/growth/accounts").contentType(MediaType.APPLICATION_JSON)
			.content("{\"externalId\":\"security-manager\",\"name\":\"Manager Account\",\"region\":\"US\",\"industry\":\"Retail\"}"))
			.andExpect(status().isCreated());
	}
}
