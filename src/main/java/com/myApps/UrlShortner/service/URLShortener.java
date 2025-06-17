package com.myApps.UrlShortner.service;

import com.myApps.UrlShortner.model.UrlRecord;
import com.myApps.UrlShortner.repository.URLRepo;
import com.myApps.UrlShortner.responsdto.UrlResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class URLShortener {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String COUNTER_KEY = "url_counter";

    private final RedisTemplate<String, Long> redisTemplate;
    private final URLRepo urlRepo;

    @Autowired
    public URLShortener(URLRepo urlRepo, RedisTemplate<String, Long> redisTemplate) {
        this.urlRepo = urlRepo;
        this.redisTemplate = redisTemplate;
    }

    public UrlResponseDto shortenUrl(String url) {
        log.info("Attempting to shorten URL: {}", url);
        Optional<UrlRecord> exists = urlRepo.findByUrl(url);
        if (exists.isPresent()) {
            log.info("URL '{}' already exists. Returning existing short code: {}", url, exists.get().getShortUrl());
            return toDto(exists.get());
        }
        // --- THIS IS THE CRITICAL SECTION TO MONITOR ---
        log.info("URL '{}' is new. Attempting to increment Redis counter: {}", url, COUNTER_KEY);
        Long Key = redisTemplate.opsForValue().increment(COUNTER_KEY);

        if (Key == null) {
            log.error("Failed to increment counter in Redis. 'increment' returned null for key '{}'. This should not happen.", COUNTER_KEY);
            throw new RuntimeException("Failed to increment counter in Redis");
        }
        // Log the exact value received from Redis
        log.info("Redis counter for '{}' incremented to: {}", COUNTER_KEY, Key);
        StringBuilder sb = new StringBuilder();
        // The base62 conversion loop:
        long tempKey =Key;
        if (tempKey == 0) { // Handle the edge case if 0 is ever passed, although with your start, it won't be
            sb.append(BASE62.charAt(0));
        } else {
            while (tempKey > 0) {
                int rem = (int) (tempKey % 62);
                sb.append(BASE62.charAt(rem));
                tempKey /= 62;
            }
        }
        String shortCode = sb.reverse().toString();
        // Log the generated shortCode
        log.info("Generated short code '{}' from counter value '{}' for URL '{}'.", shortCode, Key, url);

        // --- END CRITICAL SECTION ---

        UrlRecord record = new UrlRecord(null, url, padShortCode(shortCode), null, null, 1L);
        log.info("Saving new URLRecord: URL='{}', ShortCode='{}'", record.getUrl(), record.getShortUrl());
        UrlRecord saved = urlRepo.save(record);
        log.info("URLRecord saved successfully with ID: {}", saved.getId());

        return toDto(saved);
    }

    public UrlResponseDto updateUrl(String code, String url) {
        UrlRecord record = urlRepo.findByshortUrl(code)
                .map(r -> {
                    r.setUrl(url);
                    r.incrementAccessed(r.getAccessed());
                    return urlRepo.save(r);
                })
                .orElseThrow(() -> new RuntimeException("ShortCode not found"));

        return toDto(record);
    }

    public void Delete(String code) {
        UrlRecord record = urlRepo.findByshortUrl(code)
                .orElseThrow(() -> new RuntimeException("ShortCode does not exist"));
        urlRepo.delete(record);
        log.info("Deleted record for short code: {}", code);
    }

    public UrlResponseDto toDto(UrlRecord r) {
        return UrlResponseDto.builder()
                .shortUrl(r.getShortUrl())
                .Url(r.getUrl())
                .accessed(r.getAccessed())
                .createdAt(r.getGeneratedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
    public String padShortCode(String shortCode) {
        int targetLength = 6;
        int paddingLength = targetLength - shortCode.length();

        if (paddingLength <= 0) {
            return shortCode; // Already 6 or more characters
        }

        // Pad with '0' to the left
        return "0".repeat(paddingLength) + shortCode;
    }
}
