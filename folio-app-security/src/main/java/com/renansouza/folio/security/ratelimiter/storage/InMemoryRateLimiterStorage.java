package com.renansouza.folio.security.ratelimiter.storage;


import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of {@link RateLimiterStorage} using {@link ConcurrentHashMap}. This
 * implementation provides thread-safe operations and automatic cleanup of expired entries.
 *
 * <p>Features:
 * <ul>
 *   <li>Thread-safe operations using ConcurrentHashMap</li>
 *   <li>Automatic cleanup of expired entries every 60 seconds</li>
 *   <li>Atomic increment operations</li>
 *   <li>Zero external dependencies</li>
 * </ul>
 *
 * <p>This implementation is suitable for single-instance applications where
 * rate limiting doesn't need to be shared across multiple instances.
 *
 * @author Renan Alberto de Souza
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "folio.security.rate-limiter.storage.type", havingValue = "memory", matchIfMissing = true)
@ConditionalOnMissingBean(RateLimiterStorage.class)
public class InMemoryRateLimiterStorage implements RateLimiterStorage {

  private final ConcurrentHashMap<String, RateLimiterEntry> storage = new ConcurrentHashMap<>();

  /**
   * Creates a new in-memory storage instance and starts the cleanup scheduler. The cleanup process
   * runs every 60 seconds to remove expired entries.
   */
  public InMemoryRateLimiterStorage() {
    try (ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
        r -> {
          Thread thread = new Thread(r, "rate-limiter-cleanup");
          thread.setDaemon(true);
          return thread;
        })) {
      cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 60, 60, TimeUnit.SECONDS);
    }
  }

  @Override
  public Optional<Long> get(String key) {
    if (key == null) {
      return Optional.empty();
    }

    RateLimiterEntry entry = storage.get(key);
    if (entry == null || entry.isExpired()) {
      if (entry != null) {
        storage.remove(key, entry);
      }
      return Optional.empty();
    }

    return Optional.of(entry.value());
  }

  @Override
  public void set(String key, Long value, Duration ttl) {
    if (key == null || value == null || ttl == null) {
      throw new IllegalArgumentException("Key, value, and TTL cannot be null");
    }

    Instant expiresAt = Instant.now().plus(ttl);
    RateLimiterEntry entry = new RateLimiterEntry(value, expiresAt);
    storage.put(key, entry);
  }

  @Override
  public Long increment(String key, Long increment, Duration ttl) {
    if (key == null || increment == null || ttl == null) {
      throw new IllegalArgumentException("Key, increment, and TTL cannot be null");
    }

    return storage.compute(key, (k, existingEntry) -> {
      if (existingEntry == null || existingEntry.isExpired()) {
        // Create new entry with increment value
        return new RateLimiterEntry(increment, Instant.now().plus(ttl));
      } else {
        // Update existing entry with incremented value, keeping original expiration
        Long newValue = existingEntry.value() + increment;
        return existingEntry.withValue(newValue);
      }
    }).value();
  }

  @Override
  public boolean delete(String key) {
    if (key == null) {
      return false;
    }

    return storage.remove(key) != null;
  }

  @Override
  public boolean exists(String key) {
    if (key == null) {
      return false;
    }

    RateLimiterEntry entry = storage.get(key);
    if (entry == null) {
      return false;
    }

    if (entry.isExpired()) {
      storage.remove(key, entry);
      return false;
    }

    return true;
  }

  @Override
  public boolean expire(String key, Duration ttl) {
    if (key == null || ttl == null) {
      return false;
    }

    Instant newExpiresAt = Instant.now().plus(ttl);

    return storage.computeIfPresent(key, (k, existingEntry) -> {
      if (existingEntry.isExpired()) {
        return null; // Remove expired entry
      }
      return existingEntry.withExpiration(newExpiresAt);
    }) != null;
  }

  /**
   * Performs cleanup of expired entries. This method is called automatically by the scheduled
   * executor, but can also be called manually if needed.
   */
  public void cleanupExpiredEntries() {
    storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
  }
}