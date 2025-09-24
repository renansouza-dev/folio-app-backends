package com.renansouza.folio.security.ratelimiter.core;

/**
 * Main rate limiter interface - focused on allow/deny decision
 */
public interface RateLimiter {

  /**
   * Check if request should be allowed or not
   *
   * @param key - unique identifier for rate limiting
   * @return boolean with allow/deny decision
   */
  boolean allowed(String key);
}