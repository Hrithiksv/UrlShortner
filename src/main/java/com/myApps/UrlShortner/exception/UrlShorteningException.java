package com.myApps.UrlShortner.exception;


public class UrlShorteningException extends RuntimeException{
    public UrlShorteningException(String message) {
        super(message);
    }

    public UrlShorteningException(String message, Throwable cause) {
        super(message, cause);
    }
}
