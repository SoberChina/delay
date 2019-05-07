package com.sober.delay.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * redis entity config
 *
 * @author liweigao
 * @date 2018/12/18 下午8:11
 */
@Getter
@Configuration
public class RedisEntityConfig {

    private final RedisTemplate redisTemplate;

    private final HashOperations<String, String, String> stringHashOperations;

    private final ValueOperations<String, Integer> integerValueOperations;

    private final ValueOperations<String, String> stringStringValueOperations;

    public RedisEntityConfig(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringHashOperations = redisTemplate.opsForHash();
        this.integerValueOperations = redisTemplate.opsForValue();
        this.stringStringValueOperations = redisTemplate.opsForValue();
    }
}
