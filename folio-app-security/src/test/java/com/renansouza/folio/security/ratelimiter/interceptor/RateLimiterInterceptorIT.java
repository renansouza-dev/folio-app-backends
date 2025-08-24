package com.renansouza.folio.security.ratelimiter.interceptor;

import com.renansouza.folio.security.ratelimiter.controller.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link RateLimiterInterceptor} within Spring MVC context.
 * <p>
 * These tests verify that the interceptor works correctly when integrated with
 * Spring MVC's interceptor chain and actual HTTP request processing.
 * </p>
 */
@SpringBootTest(classes = {
        TestApplication.class,
        RateLimiterInterceptorIT.TestConfig.class,
        RateLimiterInterceptorIT.TestController.class,
})
@TestPropertySource(properties = "rate-limiter.enabled=true")
@AutoConfigureMockMvc
@DisplayName("RateLimiterInterceptor Integration Tests")
class RateLimiterInterceptorIT {

    private static final String API_USER_PATH = "/api/user";
    private static final String API_USER_RESPONSE = "API user";
    private static final String API_DATA_PATH = "/api/data";
    private static final String API_DATA_RESPONSE = "API data";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow requests to reach controller when rate limit not exceeded")
    void integrationTest_whenRateLimitNotExceeded_shouldAllowRequest() throws Exception {
        // When/Then
        mockMvc.perform(get(API_USER_PATH))
            .andExpect(status().isOk())
            .andExpect(content().string(API_USER_RESPONSE));

        mockMvc.perform(get(API_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(API_DATA_RESPONSE));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RateLimiterInterceptor rateLimiterInterceptor() {
            return new RateLimiterInterceptor();
        }

        @Bean
        public WebMvcConfigurer rateLimiterWebMvcConfigurer(RateLimiterInterceptor interceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@NonNull InterceptorRegistry registry) {
                    registry.addInterceptor(interceptor);
                }
            };
        }
    }

    @RestController
    static class TestController {

        @GetMapping(API_USER_PATH)
        public ResponseEntity<String> test() {
            return ResponseEntity.ok(API_USER_RESPONSE);
        }

        @GetMapping(API_DATA_PATH)
        public ResponseEntity<String> data() {
            return ResponseEntity.ok(API_DATA_RESPONSE);
        }
    }

}