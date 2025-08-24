package com.renansouza.folio.security.ratelimiter.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure rate limiting on REST endpoints or entire controllers.
 * <p>
 * This annotation can be applied at both method and class levels to define rate limiting
 * rules for HTTP requests. When applied at the class level, the rate limiting configuration
 * applies to all methods within the class unless overridden by method-level annotations.
 * </p>
 *
 * <h3>Usage Examples</h3>
 *
 * <h4>Method-Level Rate Limiting:</h4>
 * <pre>{@code
 * @RestController
 * public class ApiController {
 *
 *     @RateLimit(limit = 10, duration = 60,
 *                algorithm = AlgorithmType.SLIDING_WINDOW,
 *                storage = StorageType.IN_MEMORY)
 *     @GetMapping("/api/sensitive")
 *     public ResponseEntity<String> getSensitiveData() {
 *         return ResponseEntity.ok("data");
 *     }
 *
 *     @RateLimit(limit = 5, duration = 300,
 *                algorithm = AlgorithmType.TOKEN_BUCKET,
 *                storage = StorageType.REDIS)
 *     @PostMapping("/api/upload")
 *     public ResponseEntity<String> uploadFile() {
 *         return ResponseEntity.ok("uploaded");
 *     }
 * }
 * }</pre>
 *
 * <h4>Class-Level Rate Limiting:</h4>
 * <pre>{@code
 * @RateLimit(limit = 100, duration = 60,
 *            algorithm = AlgorithmType.SLIDING_WINDOW,
 *            storage = StorageType.REDIS)
 * @RestController
 * @RequestMapping("/api/v1")
 * public class V1ApiController {
 *
 *     // Inherits class-level rate limiting: 100 requests per 60 seconds
 *     @GetMapping("/data")
 *     public ResponseEntity<String> getData() {
 *         return ResponseEntity.ok("data");
 *     }
 *
 *     // Overrides class-level configuration with stricter limits
 *     @RateLimit(limit = 5, duration = 60,
 *                algorithm = AlgorithmType.FIXED_WINDOW,
 *                storage = StorageType.IN_MEMORY)
 *     @PostMapping("/admin")
 *     public ResponseEntity<String> adminAction() {
 *         return ResponseEntity.ok("admin");
 *     }
 * }
 * }</pre>
 *
 * <h3>Algorithm Types</h3>
 * <ul>
 *   <li><strong>FIXED_WINDOW:</strong> Simple time-based windows (e.g., 100 requests per minute)</li>
 *   <li><strong>SLIDING_WINDOW:</strong> Continuous time windows that slide with each request</li>
 *   <li><strong>TOKEN_BUCKET:</strong> Allows bursts up to bucket capacity, refills at steady rate</li>
 *   <li><strong>LEAKY_BUCKET:</strong> Smooths out traffic spikes by processing requests at steady rate</li>
 * </ul>
 *
 * <h3>Storage Types</h3>
 * <ul>
 *   <li><strong>IN_MEMORY:</strong> Fast, suitable for single-instance applications</li>
 *   <li><strong>REDIS:</strong> Distributed, suitable for multi-instance/cluster deployments</li>
 * </ul>
 *
 * <h3>Configuration Priority</h3>
 * <p>
 * When multiple rate limiting configurations are present, they are applied in the following order of priority:
 * </p>
 * <ol>
 *   <li>Method-level {@code @RateLimit} annotation (highest priority)</li>
 *   <li>Class-level {@code @RateLimit} annotation</li>
 *   <li>Endpoint-specific configuration in {@code application.yml}</li>
 * </ol>
 *
 * <h3>Key Generation</h3>
 * <p>
 * The rate limiting key is typically generated based on:
 * </p>
 * <ul>
 *   <li>Client IP address</li>
 *   <li>User authentication information (if available)</li>
 *   <li>Request path and HTTP method</li>
 * </ul>
 *
 * <h3>Error Handling</h3>
 * <p>
 * When rate limits are exceeded, the system responds with:
 * </p>
 * <ul>
 *   <li>HTTP Status: {@code 429 Too Many Requests}</li>
 *   <li>Response headers indicating rate limit status</li>
 *   <li>Optional retry-after header</li>
 * </ul>
 *
 * <h3>Prerequisites</h3>
 * <p>
 * For Redis storage type, ensure the following dependency is included:
 * </p>
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-redis</artifactId>
 * </dependency>
 * }</pre>
 *
 * <h3>Performance Considerations</h3>
 * <ul>
 *   <li><strong>IN_MEMORY:</strong> Fastest performance, but limited to single instance</li>
 *   <li><strong>REDIS:</strong> Slight network overhead, but supports clustering</li>
 * </ul>
 *
 * @author Renan Alberto de Souza
 * @since 1.0.0
 * @see RateLimiterProperties.AlgorithmType
 * @see RateLimiterProperties.StorageType
 * @see RateLimiterProperties
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * The maximum number of requests allowed within the specified duration.
     * <p>
     * This defines the rate limit threshold. Once this number of requests
     * is reached within the time window defined by {@link #duration()},
     * subsequent requests will be rejected with HTTP 429 status.
     * </p>
     *
     * <h4>Examples:</h4>
     * <ul>
     *   <li>{@code limit = 100} - Allow up to 100 requests</li>
     *   <li>{@code limit = 10} - Allow up to 10 requests (more restrictive)</li>
     *   <li>{@code limit = 1000} - Allow up to 1000 requests (less restrictive)</li>
     * </ul>
     *
     * @return the maximum number of requests allowed
     * @see #duration()
     */
    int limit();

    /**
     * The time window duration in seconds for the rate limit.
     * <p>
     * This defines the time period over which the {@link #limit()} is applied.
     * The interpretation of this duration depends on the selected {@link #algorithm()}.
     * </p>
     *
     * <h4>Examples:</h4>
     * <ul>
     *   <li>{@code duration = 60} - Time window of 1 minute</li>
     *   <li>{@code duration = 3600} - Time window of 1 hour</li>
     *   <li>{@code duration = 300} - Time window of 5 minutes</li>
     * </ul>
     *
     * <h4>Algorithm-Specific Behavior:</h4>
     * <ul>
     *   <li><strong>FIXED_WINDOW:</strong> Fixed time periods (e.g., every 60 seconds)</li>
     *   <li><strong>SLIDING_WINDOW:</strong> Rolling time window that moves with each request</li>
     *   <li><strong>TOKEN_BUCKET:</strong> Time interval for token refill</li>
     *   <li><strong>LEAKY_BUCKET:</strong> Time interval for request processing</li>
     * </ul>
     *
     * @return the duration in seconds
     * @see #limit()
     * @see #algorithm()
     */
    int duration();

    /**
     * The rate limiting algorithm to use for this endpoint.
     * <p>
     * Different algorithms provide different behaviors and are suitable
     * for different use cases:
     * </p>
     *
     * <h4>Algorithm Types:</h4>
     * <ul>
     *   <li><strong>FIXED_WINDOW:</strong> Simple and efficient, but allows traffic bursts at window boundaries</li>
     *   <li><strong>SLIDING_WINDOW:</strong> More accurate rate limiting, prevents boundary bursts</li>
     *   <li><strong>TOKEN_BUCKET:</strong> Allows controlled bursts while maintaining average rate</li>
     *   <li><strong>LEAKY_BUCKET:</strong> Smooths traffic by processing at steady rate</li>
     * </ul>
     *
     * <h4>Use Case Recommendations:</h4>
     * <ul>
     *   <li><strong>FIXED_WINDOW:</strong> General API endpoints, simple rate limiting</li>
     *   <li><strong>SLIDING_WINDOW:</strong> Critical endpoints requiring precise control</li>
     *   <li><strong>TOKEN_BUCKET:</strong> File uploads, batch operations that benefit from bursts</li>
     *   <li><strong>LEAKY_BUCKET:</strong> Real-time systems requiring steady processing rates</li>
     * </ul>
     *
     * @return the rate limiting algorithm to use
     * @see RateLimiterProperties.AlgorithmType
     * @see #limit()
     * @see #duration()
     */
    RateLimiterProperties.AlgorithmType algorithm();

    /**
     * The storage backend to use for tracking rate limit state.
     * <p>
     * The choice of storage affects performance, scalability, and deployment requirements:
     * </p>
     *
     * <h4>Storage Types:</h4>
     * <ul>
     *   <li><strong>IN_MEMORY:</strong> Fastest performance, suitable for single-instance applications</li>
     *   <li><strong>REDIS:</strong> Distributed storage, required for multi-instance deployments</li>
     * </ul>
     *
     * <h4>Selection Guidelines:</h4>
     * <ul>
     *   <li><strong>Single Instance:</strong> Use IN_MEMORY for best performance</li>
     *   <li><strong>Multiple Instances/Clusters:</strong> Use REDIS for shared state</li>
     *   <li><strong>High Availability:</strong> Use REDIS with proper clustering</li>
     * </ul>
     *
     * <h4>Prerequisites:</h4>
     * <p>
     * When using REDIS storage, ensure Redis dependencies are included:
     * </p>
     * <pre>{@code
     * <dependency>
     *     <groupId>org.springframework.boot</groupId>
     *     <artifactId>spring-boot-starter-data-redis</artifactId>
     * </dependency>
     * }</pre>
     *
     * @return the storage backend to use
     * @see RateLimiterProperties.StorageType
     * @see #algorithm()
     */
    RateLimiterProperties.StorageType storage();

}