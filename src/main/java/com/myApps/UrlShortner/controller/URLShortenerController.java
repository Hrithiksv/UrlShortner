package com.myApps.UrlShortner.controller;

import com.myApps.UrlShortner.responsdto.UrlResponseDto;
import com.myApps.UrlShortner.service.URLResolver;
import com.myApps.UrlShortner.service.URLShortener;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shortener")

public class URLShortenerController {

    private final URLShortener urlShortener;
    private final URLResolver urlResolver;

    public URLShortenerController(URLShortener urlShortener, URLResolver resolver) {
        this.urlShortener = urlShortener;
        this.urlResolver = resolver;
    }

    @PostMapping
    @RateLimiter(name = "urlShortener")
    public ResponseEntity<UrlResponseDto> createShortUrl(@RequestParam("url") String Url){
        UrlResponseDto urlResponseDto = urlShortener.shortenUrl(Url);

        return ResponseEntity.status(201).body(urlResponseDto);
    }

    @GetMapping("/resolve/{code}")
    @RateLimiter(name = "urlShortener")
    public ResponseEntity<UrlResponseDto> getShortUrl(@PathVariable("code") String code){
        UrlResponseDto resolve = urlResolver.resolve(code);
        return ResponseEntity.ok().body(resolve);
    }

    @PutMapping("/{code}")
    @RateLimiter(name = "urlShortener")
    public ResponseEntity<UrlResponseDto> updateUrl(@PathVariable("code") String shortCode, @RequestParam("url") String url){
        UrlResponseDto urlResponseDto = urlShortener.updateUrl(shortCode, url);

        return ResponseEntity.status(204).body(urlResponseDto);
    }

    @DeleteMapping("/{code}")
    @RateLimiter(name = "urlShortener")
    public ResponseEntity<UrlResponseDto> deleteUrl(@PathVariable("code") String code){
        urlShortener.Delete(code);
        return ResponseEntity.ok().build();
    }
}
