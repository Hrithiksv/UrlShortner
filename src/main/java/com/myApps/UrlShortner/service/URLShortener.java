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
    private final RedisCounterService redisCounter;

    private final URLRepo urlRepo;

    @Autowired
    public URLShortener(URLRepo urlRepo, RedisTemplate<String, Long> redisTemplate, RedisCounterService redisCounterService) {
        this.urlRepo = urlRepo;

        this.redisCounter = redisCounterService;
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
        Long Key = redisCounter.generateKey();

        // Log the exact value received from Redis
        log.info("Redis counter for '{}' incremented to: {}", COUNTER_KEY, Key);

        String shortCode = encodeToBase62(Key);
        // Log the generated shortCode
        log.info("Generated short code '{}' from counter value '{}' for URL '{}'.", shortCode, Key, url);

        // --- END CRITICAL SECTION ---

        UrlRecord urlRecorde = new UrlRecord(null, url, padShortCode(shortCode), null, null, 1L);
        log.info("Saving new URLRecord: URL='{}', ShortCode='{}'", urlRecorde.getUrl(), urlRecorde.getShortUrl());
        UrlRecord saved = urlRepo.save(urlRecorde);
        log.info("URLRecord saved successfully with ID: {}", saved.getId());

        return toDto(saved);
    }

    private static String encodeToBase62(Long Key) {
        StringBuilder sb = new StringBuilder();
        // The base62 conversion loop:
        long tempKey = Key;
        if (tempKey == 0) { // Handle the edge case if 0 is ever passed, although with your start, it won't be
            throw new RuntimeException("generated Key is : "+tempKey);
        } else {
            while (tempKey > 0) {
                int rem = (int) (tempKey % 62);
                sb.append(BASE62.charAt(rem));
                tempKey /= 62;
            }
        }
        return sb.reverse().toString();
    }

    public UrlResponseDto updateUrl(String code, String url) {
        UrlRecord urlRecorde = urlRepo.findByshortUrl(code)
                .map(r -> {
                    r.setUrl(url);
                    r.incrementAccessed();
                    return urlRepo.save(r);
                })
                .orElseThrow(() -> new RuntimeException("ShortCode not found"));

        return toDto(urlRecorde);
    }

    public void DeleteUrl(String code) {
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

        // Pad with '= ' to the left
        return "=".repeat(paddingLength) + shortCode;
    }
}
