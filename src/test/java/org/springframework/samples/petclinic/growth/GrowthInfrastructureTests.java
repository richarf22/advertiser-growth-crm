package org.springframework.samples.petclinic.growth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.growth.service.OutboxPublisherService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.profiles.active=postgres,spring-data-jpa,infra",
	"petclinic.security.enable=false",
	"growth.outbox.enabled=true",
	"growth.outbox.poll-ms=600000"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class GrowthInfrastructureTests {
	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.3")
		.withDatabaseName("growthcrm").withUsername("growthcrm").withPassword("growthcrm");

	@Container
	static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
		.withExposedPorts(6379);

	@Container
	static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.0.0"));

	@DynamicPropertySource
	static void infrastructure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private OutboxPublisherService outbox;

	@Test
	void executesCoreRestFlowAgainstPostgresRedisAndKafka() throws Exception {
		mvc.perform(get("/api/growth/accounts/1")).andExpect(status().isOk());
		String opportunityJson = mvc.perform(post("/api/growth/accounts/1/opportunities")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Container Campaign\",\"valueCents\":500000,\"currency\":\"USD\"}"))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		long opportunityId = Long.parseLong(opportunityJson.replaceAll(".*\"id\":(\\d+).*", "$1"));

		mvc.perform(post("/api/growth/opportunities/{id}/transitions", opportunityId)
				.header("Idempotency-Key", "container-transition-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"toStage\":\"QUALIFIED\",\"actor\":\"testcontainers\"}"))
			.andExpect(status().isOk());

		outbox.publishBatch();
		mvc.perform(get("/api/growth/audit-events"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].status").value("PUBLISHED"));
		mvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}
}
