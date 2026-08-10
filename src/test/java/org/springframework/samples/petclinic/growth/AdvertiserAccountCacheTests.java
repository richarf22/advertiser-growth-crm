package org.springframework.samples.petclinic.growth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.AccountRequest;
import org.springframework.samples.petclinic.growth.config.GrowthInfrastructureConfig;
import org.springframework.samples.petclinic.growth.service.AdvertiserAccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
	"spring.profiles.active=h2,spring-data-jpa",
	"spring.datasource.url=jdbc:h2:mem:growthcache;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	"petclinic.security.enable=false",
	"spring.cache.type=simple",
	"growth.outbox.enabled=false"
})
class AdvertiserAccountCacheTests {
	@Autowired
	private AdvertiserAccountService accounts;

	@Autowired
	private CacheManager cacheManager;

	@Test
	void updateEvictsCachedAccountRead() {
		accounts.get(1L);
		Cache cache = cacheManager.getCache("advertiserAccounts");
		assertThat(cache).isNotNull();
		assertThat(cache.get(1L)).isNotNull();

		accounts.update(1L, new AccountRequest("adv-demo-001", "Updated Demo", "US", "Retail"));

		assertThat(cache.get(1L)).isNull();
		assertThat(accounts.get(1L).getName()).isEqualTo("Updated Demo");
	}

	@Test
	void cacheFailureHandlerKeepsDatabasePathAvailable() {
		Cache unavailableCache = mock(Cache.class);
		when(unavailableCache.getName()).thenReturn("advertiserAccounts");

		assertThatCode(() -> new GrowthInfrastructureConfig().errorHandler()
			.handleCacheGetError(new IllegalStateException("redis unavailable"), unavailableCache, 1L))
			.doesNotThrowAnyException();
	}
}
