package com.myApps.UrlShortner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 503 Service Unavailable is a good choice for when a backend dependency fails.
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RedisServiceException extends UrlShorteningException {
    public RedisServiceException(String message) {
        super(message);
    }
    public RedisServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
