package com.lpu.auth_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    // This method defines a bean for the StringRedisTemplate, which is a helper class that simplifies Redis operations. 
    // It takes a RedisConnectionFactory as a parameter, which is used to create connections to the Redis server.
	@Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
