package org.springframework.samples.petclinic.growth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class GrowthInfrastructureConfig implements CachingConfigurer {
	private static final Logger logger = LoggerFactory.getLogger(GrowthInfrastructureConfig.class);

	@Override
	public CacheErrorHandler errorHandler() {
		return new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
				logger.warn("Cache read failed for cache={} key={}; continuing with database", cache.getName(), key);
			}
			@Override
			public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
				logger.warn("Cache write failed for cache={} key={}; database result remains authoritative", cache.getName(), key);
			}
			@Override
			public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
				logger.warn("Cache eviction failed for cache={} key={}", cache.getName(), key);
			}
			@Override
			public void handleCacheClearError(RuntimeException exception, Cache cache) {
				logger.warn("Cache clear failed for cache={}", cache.getName());
			}
		};
	}
}
