package com.example.blogapi.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
                return new LettuceConnectionFactory();
        }

        @Bean
        public ObjectMapper redisObjectMapper() {
                ObjectMapper mapper = new ObjectMapper();

                mapper.registerModule(new JavaTimeModule());

                mapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                                .allowIfBaseType(Object.class)
                                                .build(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return mapper;
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();

                template.setConnectionFactory(connectionFactory);

                // Key serialization (String)
                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                // Value serialization (JSON)
                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper());
                template.setValueSerializer(serializer);
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();

                return template;
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)) // Default TTL: 10 minutes
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new GenericJackson2JsonRedisSerializer(
                                                                                redisObjectMapper())))
                                .disableCachingNullValues(); // Không cache null values

                // Custom configurations cho từng cache
                RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig);

                // Posts cache: 10 minutes TTL
                builder.withCacheConfiguration("posts",
                                defaultConfig.entryTtl(Duration.ofMinutes(10)));

                // Users cache: 30 minutes TTL (user info ít thay đổi)
                builder.withCacheConfiguration("users",
                                defaultConfig.entryTtl(Duration.ofMinutes(30)));

                // Comments cache: 5 minutes TTL (có thể thay đổi thường xuyên)
                builder.withCacheConfiguration("comments",
                                defaultConfig.entryTtl(Duration.ofMinutes(5)));

                // Search results cache: 2 minutes TTL (data tạm thời)
                builder.withCacheConfiguration("searchResults",
                                defaultConfig.entryTtl(Duration.ofMinutes(2)));

                return builder.build();
        }

        /**
         * Register CustomCacheErrorHandler for graceful degradation.
         * When Redis is down, the app continues to work by falling back to DB queries.
         */
        @Override
        public CacheErrorHandler errorHandler() {
                return new CustomCacheErrorHandler();
        }

}
