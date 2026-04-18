package org.cheong.fittrackermiddleware.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableCaching
public class CacheConfigurationTest {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("access_token");
    }
}
