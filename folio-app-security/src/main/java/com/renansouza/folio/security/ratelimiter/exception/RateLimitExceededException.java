package com.renansouza.folio.security.ratelimiter.exception;

/**
 * Exception class to be capture by webapi's to handle.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super();
    }

}