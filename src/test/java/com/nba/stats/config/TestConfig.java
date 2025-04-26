package com.nba.stats.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.sql.DataSource;
import java.time.Duration;

@TestConfiguration
@Profile("test")
@EnableCaching
public class TestConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30)) // Shorter TTL for tests to ensure cache eviction works
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .transactionAware() // Make cache operations participate in transactions
                .build();
    }
    
    /**
     * Special configuration for tests to ensure cache operations are more reliable
     * This ensures that cache operations will be properly synchronized with DB operations
     */
    @Bean
    @Profile("test")
    public Object cacheTestSettings() {
        // These settings help ensure caching operations in tests behave more predictably
        System.setProperty("spring.cache.type", "redis");
        System.setProperty("spring.cache.redis.time-to-live", "30000"); // 30 seconds TTL for tests
        System.setProperty("spring.cache.redis.enable-statistics", "true");
        
        return new Object(); // Just a placeholder
    }
}