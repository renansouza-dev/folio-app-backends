package com.renansouza.folio.security.ratelimiter.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RateLimiterInterceptor}.
 * <p>
 * These tests verify the behavior of the rate limiting interceptor under various scenarios
 * including normal requests, rate limit violations, and edge cases.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterInterceptor Tests")
class RateLimiterInterceptorTest {

    private RateLimiterInterceptor interceptor;

    @Mock
    private Object handler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimiterInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Should allow request when rate limit is not exceeded")
    void preHandle_whenRateLimitNotExceeded_shouldReturnTrue() {
        // Given
        request.setRequestURI("/api/test");
        request.setRemoteAddr("192.168.1.1");
        request.setMethod("GET");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void preHandle_withDifferentHttpMethods_shouldAllowAll() {
        // Given
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};

        for (String method : methods) {
            // Given
            request.setMethod(method);
            request.setRequestURI("/api/endpoint");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("Should handle different client IP addresses")
    void preHandle_withDifferentClientIPs_shouldAllowAll() {
        // Given
        String[] ipAddresses = {
                "192.168.1.1",
                "10.0.0.1",
                "127.0.0.1",
                "203.0.113.1"
        };

        for (String ip : ipAddresses) {
            // Given
            request.setRemoteAddr(ip);
            request.setRequestURI("/api/test");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("Should handle different request paths")
    void preHandle_withDifferentPaths_shouldAllowAll() {
        // Given
        String[] paths = {
                "/api/users",
                "/api/orders",
                "/health",
                "/metrics",
                "/admin/config"
        };

        for (String path : paths) {
            // Given
            request.setRequestURI(path);
            request.setRemoteAddr("192.168.1.1");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertThat(result).isTrue();
        }
    }

}