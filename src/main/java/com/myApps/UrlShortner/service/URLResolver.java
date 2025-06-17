package com.myApps.UrlShortner.service;

import com.myApps.UrlShortner.model.UrlRecord;
import com.myApps.UrlShortner.repository.URLRepo;
import com.myApps.UrlShortner.responsdto.UrlResponseDto;
import org.springframework.stereotype.Service;

@Service
public class URLResolver {

    private final URLRepo urlRepo;

    public URLResolver(URLRepo urlRepo) {
        this.urlRepo = urlRepo;
    }

    public UrlResponseDto resolve(String code){
        UrlRecord record = urlRepo.findByshortUrl(code)
                .map(r->{
                    r.incrementAccessed(r.getAccessed());
                    return urlRepo.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Url doesn't exist"));

        return UrlResponseDto.builder()
                .Url(record.getUrl())
                .shortUrl(record.getShortUrl())
                .updatedAt(record.getUpdatedAt())
                .createdAt(record.getGeneratedAt())
                .accessed(record.getAccessed()+1)
                .build();
    }
}
