package com.myApps.UrlShortner.service;


import com.myApps.UrlShortner.exception.RedisServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
public class RedisCounterService {

    private final RedisTemplate<String, Long> redisTemplate;
    private static final String COUNTER_KEY = "url_counter";

    public RedisCounterService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Retryable(
            retryFor = { RedisServiceException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) // 200ms delay between retries
    )
    public Long generateKey(){
        try {
            Long key = redisTemplate.opsForValue().increment(COUNTER_KEY);
            if(key == null){
                throw new RedisServiceException("Failed to increment counter in Redis. 'increment' returned null.");
            }
            log.info("Redis counter '{}' incremented to: {}", COUNTER_KEY, key);
            return key;
        }catch (Exception e){
            log.error("Failed to connect or increment Redis counter on this attempt.", e);
            throw new RedisServiceException("Error while incrementing Redis counter", e);
        }
    }
}
