package com.renansouza.folio.security.ratelimiter.config;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Rate Limiter library.
 * <p>
 * This class binds to properties defined under the {@code rate-limiter} prefix in the application's
 * {@code application.yaml} or {@code application.properties}. It provides global configuration for
 * rate limiting as well as per-route rules.
 * </p>
 *
 * <h2>Example Configuration</h2>
 * <pre>
 * rate-limiter:
 *   enabled: true
 *   key: ip
 *   algorithm: fixed-window
 *   storage: in-memory
 *   rules:
 *     - path: /api/orders/**
 *       method: GET
 *       limit: 10
 *       duration: 60
 *     - path: /api/payments/**
 *       method: POST
 *       limit: 5
 *       duration: 120
 * </pre>
 *
 * <h2>Properties</h2>
 * <ul>
 *   <li>{@code enabled} – Enables or disables rate limiting globally.</li>
 *   <li>{@code key} – Defines the key used for identifying clients (e.g., {@code ip}, {@code user}).</li>
 *   <li>{@code algorithm} – Default rate limiting algorithm to use if not overridden in rules.</li>
 *   <li>{@code storage} – Storage type to persist rate limiting state ({@code in-memory} or {@code redis}).</li>
 *   <li>{@code rules} – A set of rule definitions that specify rate limits for individual routes.</li>
 * </ul>
 * <p>
 *  @author Renan Alberto de Souza
 *  @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

  /**
   * Whether the rate limiter is enabled. If set to {@code false}, all requests are allowed without
   * checks.
   */
  private boolean enabled;

  /**
   * The client key type used for identifying requests. Common values include:
   * <ul>
   *   <li>{@code ip} – Use the client IP address.</li>
   *   <li>{@code user} – Use the authenticated user identifier.</li>
   *   <li>{@code header:X-Api-Key} – Use a custom header value.</li>
   * </ul>
   */
  private KeyType key;

  /**
   * The default algorithm used for rate limiting. Can be overridden by individual {@link Rule}
   * objects.
   */
  private AlgorithmType algorithm;

  /**
   * The storage backend used to persist rate limiting state. Options are in-memory (local JVM only)
   * or Redis (distributed).
   */
  private StorageType storage;

  /**
   * A set of per-route rules that override global configuration. Each rule defines its own path,
   * method, limit, and duration.
   */
  private Set<Rule> rules = new HashSet<>();

  /**
   * Supported rate limiting key.
   */
  public enum KeyType {
    IP
  }

  /**
   * Supported rate limiting algorithms.
   */
  public enum AlgorithmType {
    FIXED_WINDOW, TOKEN_BUCKET, SLIDING_WINDOW, LEAKY_BUCKET
  }

  /**
   * Supported storage backends for rate limiting state.
   */
  public enum StorageType {
    IN_MEMORY, REDIS
  }

  /**
   * Defines a single rate limiting rule for a specific route.
   */
  @Data
  public static class Rule {

    /**
     * The request path that this rule applies to. Supports exact matching (e.g., {@code /api/**}).
     */
    private String path;

    /**
     * The HTTP method that this rule applies to (e.g., {@code GET}, {@code POST}, {@code ALL}).
     */
    private String method;

    /**
     * The maximum number of allowed requests within the defined {@code duration}.
     */
    private int limit;

    /**
     * The time window (in seconds) for which the {@code limit} applies.
     */
    private int duration;

  }

}