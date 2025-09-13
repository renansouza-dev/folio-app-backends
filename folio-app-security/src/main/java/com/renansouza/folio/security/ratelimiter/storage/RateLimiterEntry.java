package com.renansouza.folio.security.ratelimiter.storage;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a storage entry for rate limiting data with expiration support.
 * This class encapsulates both the value and its expiration time for
 * efficient in-memory storage management.
 *
 * @author Renan Alberto de Souza
 * @since 1.0.0
 */
record RateLimiterEntry(Long value, Instant expiresAt) {

    /**
     * Creates a new rate limiter entry with the specified value and expiration time.
     *
     * @param value     the stored value, must not be null
     * @param expiresAt the expiration timestamp, must not be null
     * @throws IllegalArgumentException if value or expiresAt is null
     */
    RateLimiterEntry(Long value, Instant expiresAt) {
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration time cannot be null");
    }

    /**
     * Checks if this entry has expired based on the current time.
     *
     * @return true if the entry has expired, false otherwise
     */
    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Creates a new entry with an updated value but the same expiration time.
     *
     * @param newValue the new value to store
     * @return a new RateLimiterEntry with the updated value
     */
    RateLimiterEntry withValue(Long newValue) {
        return new RateLimiterEntry(newValue, this.expiresAt);
    }

    /**
     * Creates a new entry with an updated expiration time but the same value.
     *
     * @param newExpiresAt the new expiration time
     * @return a new RateLimiterEntry with the updated expiration time
     */
    RateLimiterEntry withExpiration(Instant newExpiresAt) {
        return new RateLimiterEntry(this.value, newExpiresAt);
    }
}