package com.mac.boilerplate.config;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.entities.dto.TaskResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

@EnableCaching
@Configuration
@ConditionalOnProperty(name = "task.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            TaskProperties properties,
            ObjectMapper objectMapper) {
        var serializer = new JacksonJsonRedisSerializer<>(objectMapper, TaskResponse.class);
        var configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.cache().ttl())
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        var writer = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.scan(1000));
        return RedisCacheManager.builder(writer).cacheDefaults(configuration).build();
    }
}
