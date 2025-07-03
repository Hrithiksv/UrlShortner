package com.myApps.UrlShortner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ShortCodeGenerationException extends UrlShorteningException {
    public ShortCodeGenerationException(String message) {
        super(message);
    }
    public ShortCodeGenerationException(String message,Throwable cause){ super(message,cause);}
}
