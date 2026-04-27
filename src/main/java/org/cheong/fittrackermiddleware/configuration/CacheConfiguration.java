package org.cheong.fittrackermiddleware.configuration;

import org.cheong.fittrackermiddleware.dto.TokenResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author cheong
 * Configuration for cache
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * CacheManager for Redis
     * @param redisConnectionFactory redisConnectionFactory
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration("access_token", getTokenCacheConfiguration())
                .withCacheConfiguration("user_exists", getUserExistsFlagCacheConfiguration())
                .build();

    }

    /**
     * Cache configuration for access token
     * @return RedisCacheConfiguration
     */
    public RedisCacheConfiguration getTokenCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(TokenResponse.class))
                );
    }

    /**
     * Cache configuration for user exists flag
     * @return RedisCacheConfiguration
     */
    public RedisCacheConfiguration getUserExistsFlagCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(7))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(Boolean.class))
                );
    }
}
