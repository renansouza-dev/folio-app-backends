package com.renansouza.folio.security.ratelimiter.config;

import com.renansouza.folio.security.ratelimiter.core.RateLimiter;
import com.renansouza.folio.security.ratelimiter.core.keyextractor.KeyExtractor;
import com.renansouza.folio.security.ratelimiter.interceptor.RateLimiterInterceptor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autoconfiguration class for setting up rate limiting functionality in a Spring Boot application.
 * <p>
 * This configuration class automatically configures the necessary components for rate limiting when
 * the rate limiter feature is enabled. It sets up web MVC interceptors to handle rate limiting at
 * the request level.
 * </p>
 *
 * <h3>Configuration Properties</h3>
 * <p>
 * Rate limiting behavior is controlled by properties with the prefix {@code rate-limiter}. The
 * configuration is enabled by default but can be disabled by setting
 * {@code rate-limiter.enabled=false}.
 * </p>
 *
 * <h3>Conditional Activation</h3>
 * <p>
 * This autoconfiguration is conditionally activated based on:
 * </p>
 * <ul>
 *   <li>The presence of {@code rate-limiter.enabled} property set to {@code true}</li>
 *   <li>If the property is missing, the configuration is enabled by default ({@code matchIfMissing = true})</li>
 * </ul>
 *
 * <h3>Beans Provided</h3>
 * <p>
 * This configuration provides the following Spring beans:
 * </p>
 * <ul>
 *   <li>{@link WebMvcConfigurer} - Configures web MVC interceptors for rate limiting</li>
 * </ul>
 *
 * <h3>Dependencies</h3>
 * <p>
 * This configuration expects a {@link HandlerInterceptor} bean to be available in the
 * application context, which will be automatically registered as a web MVC interceptor.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * # application.properties
 * rate-limiter.enabled=true
 * rate-limiter.default-limit=100
 * rate-limiter.window-size=60s
 * }</pre>
 *
 * @author Renan Alberto de Souza
 * @see RateLimiterProperties
 * @see WebMvcConfigurer
 * @see HandlerInterceptor
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimiterProperties.class)
@ConditionalOnProperty(prefix = "rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimiterAutoConfiguration {

  /**
   * Creates and configures a {@link HandlerInterceptor} bean for rate limiting HTTP requests.
   * <p>
   * This interceptor is automatically registered with Spring MVC to intercept incoming HTTP
   * requests and apply rate limiting logic based on the configured rate limiting strategy. The
   * interceptor will be applied to all requests handled by the Spring MVC dispatcher servlet.
   * </p>
   * <p>
   * The bean is created conditionally using {@link ConditionalOnMissingBean}, meaning it will only
   * be instantiated if no other {@link HandlerInterceptor} bean of the same type already exists in
   * the application context. This allows applications to provide their own custom rate limiter
   * interceptor implementation if needed.
   * </p>
   *
   * @param rateLimiter           the rate limiting algorithm implementation that determines whether
   *                              requests should be allowed or denied
   * @param keyExtractor          the strategy for extracting unique identifiers from HTTP requests
   *                              (e.g., IP address, user ID, custom headers)
   * @param rateLimiterProperties the configuration properties containing rate limiting settings
   *                              such as limits, time windows, and algorithm-specific parameters
   * @return a new instance of {@link RateLimiterInterceptor} configured with the provided
   * dependencies
   * @see RateLimiterInterceptor
   * @see RateLimiter
   * @see KeyExtractor
   * @see RateLimiterProperties
   * @see HandlerInterceptor
   * @see ConditionalOnMissingBean
   * @since 1.0.0
   */
  @Bean
  @ConditionalOnMissingBean
  public HandlerInterceptor rateLimiterInterceptor(RateLimiter rateLimiter,
      KeyExtractor keyExtractor, RateLimiterProperties rateLimiterProperties) {
    return new RateLimiterInterceptor(rateLimiter, keyExtractor, rateLimiterProperties);
  }

  /**
   * Configures the Web MVC framework to include rate limiting interceptors.
   * <p>
   * This bean creates a {@link WebMvcConfigurer} that registers the provided
   * {@link HandlerInterceptor} with the Spring MVC interceptor registry. The interceptor will be
   * applied to all HTTP requests handled by the application.
   * </p>
   *
   * @param interceptor the rate limiting handler interceptor to be registered. This should
   *                    typically be a rate limiting interceptor that checks request rates and
   *                    blocks excessive requests.
   * @return a configured {@link WebMvcConfigurer} that registers the rate limiting interceptor
   * @throws IllegalArgumentException if the interceptor parameter is null
   * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
   * @see HandlerInterceptor
   * @since 1.0.0
   */
  @Bean
  public WebMvcConfigurer webMvcConfigurer(HandlerInterceptor interceptor) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
      }
    };
  }

}