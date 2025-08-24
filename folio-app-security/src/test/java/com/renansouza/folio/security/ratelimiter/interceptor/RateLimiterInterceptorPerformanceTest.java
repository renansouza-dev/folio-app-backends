package com.renansouza.folio.security.ratelimiter.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance and concurrency tests for {@link RateLimiterInterceptor}.
 * <p>
 * These tests verify that the interceptor performs well under load and
 * handles concurrent requests correctly.
 * </p>
 */
class RateLimiterInterceptorPerformanceTest {

    private final RateLimiterInterceptor interceptor = new RateLimiterInterceptor();

    @RepeatedTest(100)
    @DisplayName("Should handle single requests efficiently")
    void performanceTest_singleRequests_shouldBeEfficient() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();

        request.setRequestURI("/api/test");
        request.setRemoteAddr("192.168.1.1");

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        boolean result = interceptor.preHandle(request, response, handler);

        stopWatch.stop();

        // Then
        assertThat(result).isTrue();
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(10);
    }

    @Test
    @DisplayName("Should handle concurrent requests safely")
    void concurrencyTest_multipleThreads_shouldHandleConcurrency() throws InterruptedException {
        // Given
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger totalRequests = new AtomicInteger(0);
        int numberOfRequests = 1000;
        boolean terminated;

        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            // When
            for (int i = 0; i < numberOfRequests; i++) {
                executor.submit(() -> {
                    try {
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        MockHttpServletResponse response = new MockHttpServletResponse();
                        Object handler = new Object();

                        request.setRequestURI("/api/concurrent-test");
                        request.setRemoteAddr("192.168.1." + Thread.currentThread().threadId());

                        totalRequests.incrementAndGet();
                        boolean result = interceptor.preHandle(request, response, handler);

                        if (result) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Do nothing
                    }
                });
            }

            executor.shutdown();
            terminated = executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        // Then
        assertThat(terminated).isTrue();
        assertThat(totalRequests.get()).isEqualTo(numberOfRequests);
        assertThat(successCount.get()).isEqualTo(numberOfRequests);

    }

}