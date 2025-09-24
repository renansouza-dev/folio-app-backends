package com.renansouza.folio.security.ratelimiter.storage;

import java.time.Duration;
import java.util.Optional;

/**
 * Storage abstraction for rate limiter data. Provides operations to store and retrieve rate
 * limiting information across different storage backends (in-memory, Redis, etc.).
 *
 * @author Renan Alberto de Souza
 * @since 1.0.0
 */
public interface RateLimiterStorage {

  /**
   * Retrieves the current value for a given key.
   *
   * @param key the unique identifier for the rate limit entry
   * @return the current value wrapped in Optional, or empty if key doesn't exist
   */
  Optional<Long> get(String key);

  /**
   * Sets a value for a key with an expiration time. If the key already exists, it will be
   * overwritten.
   *
   * @param key   the unique identifier for the rate limit entry
   * @param value the value to store
   * @param ttl   time-to-live for the entry
   */
  void set(String key, Long value, Duration ttl);

  /**
   * Increments the value for a key by the specified amount. If the key doesn't exist, it will be
   * created with the increment value.
   *
   * @param key       the unique identifier for the rate limit entry
   * @param increment the amount to increment by
   * @param ttl       time-to-live for the entry (only applied if key is created)
   * @return the new value after increment
   */
  Long increment(String key, Long increment, Duration ttl);

  /**
   * Removes a key and its associated value from storage.
   *
   * @param key the unique identifier to remove
   * @return true if the key was removed, false if it didn't exist
   */
  boolean delete(String key);

  /**
   * Checks if a key exists in storage.
   *
   * @param key the unique identifier to check
   * @return true if the key exists, false otherwise
   */
  boolean exists(String key);

  /**
   * Sets the expiration time for an existing key.
   *
   * @param key the unique identifier
   * @param ttl time-to-live for the entry
   * @return true if expiration was set, false if key doesn't exist
   */
  boolean expire(String key, Duration ttl);
}