package com.renansouza.folio.security.ratelimiter.core.keyextractor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Key extractor - binds request to storage key
 */
public interface KeyExtractor {

    /**
     * Extract unique key from request for rate limiting
     */
    String extractKey(HttpServletRequest request);

}