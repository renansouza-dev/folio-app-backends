package com.renansouza.folio.security.ratelimiter.interceptor;

import com.renansouza.folio.security.ratelimiter.config.RateLimiterProperties;
import com.renansouza.folio.security.ratelimiter.core.RateLimiter;
import com.renansouza.folio.security.ratelimiter.core.keyextractor.KeyExtractor;
import com.renansouza.folio.security.ratelimiter.exception.RateLimitExceededException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("Unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterInterceptor Tests")
class RateLimiterInterceptorTest {

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private KeyExtractor keyExtractor;

    @Mock
    private RateLimiterProperties rateLimiterProperties;

    @Mock
    private HandlerMethod handlerMethod;

    @Mock
    private Object nonHandlerMethodObject;

    @InjectMocks
    private RateLimiterInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Should return true when rate limiter is disabled")
    void preHandle_whenRateLimiterDisabled_shouldReturnTrue() {
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor, never()).extractKey(any());
        verify(rateLimiter, never()).allowed(any());
        verifyNoMoreInteractions(keyExtractor, rateLimiter);
    }

    @Test
    @DisplayName("Should return true when handler is not a HandlerMethod")
    void preHandle_whenHandlerIsNotHandlerMethod_shouldReturnTrue() {
        // When
        boolean result = interceptor.preHandle(request, response, nonHandlerMethodObject);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor, never()).extractKey(any());
        verify(rateLimiter, never()).allowed(any());
        verifyNoMoreInteractions(keyExtractor, rateLimiter);
    }

    @Test
    @DisplayName("Should return true when both conditions fail - rate limiter disabled AND handler not HandlerMethod")
    void preHandle_whenBothConditionsFail_shouldReturnTrue() {
        // When
        boolean result = interceptor.preHandle(request, response, nonHandlerMethodObject);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor, never()).extractKey(any());
        verify(rateLimiter, never()).allowed(any());
    }

    @Test
    @DisplayName("Should return true when rate limit is not exceeded")
    void preHandle_whenRateLimitNotExceeded_shouldReturnTrue() {
        // Given
        String extractedKey = "user:192.168.1.1:/api/test";
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(extractedKey);
        when(rateLimiter.allowed(extractedKey)).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor).extractKey(request);
        verify(rateLimiter).allowed(extractedKey);
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when rate limit is exceeded")
    void preHandle_whenRateLimitExceeded_shouldThrowException() {
        // Given
        String extractedKey = "user:192.168.1.1:/api/test";
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(extractedKey);
        when(rateLimiter.allowed(extractedKey)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RateLimitExceededException.class);

        verify(keyExtractor).extractKey(request);
        verify(rateLimiter).allowed(extractedKey);
    }

    @Test
    @DisplayName("Should return true and log warning when key extraction throws IllegalArgumentException")
    void preHandle_whenKeyExtractionThrowsIllegalArgumentException_shouldReturnTrueAndLogWarning() {
        // Given
        String errorMessage = "Invalid request format";
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenThrow(new IllegalArgumentException(errorMessage));

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor).extractKey(request);
        verify(rateLimiter, never()).allowed(any());
    }

    @Test
    @DisplayName("Should handle null key from extractor gracefully")
    void preHandle_whenKeyExtractorReturnsNull_shouldHandleGracefully() {
        // Given
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(null);
        when(rateLimiter.allowed(null)).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor).extractKey(request);
        verify(rateLimiter).allowed(null);
    }

    @Test
    @DisplayName("Should handle empty key from extractor")
    void preHandle_whenKeyExtractorReturnsEmptyString_shouldProcessNormally() {
        // Given
        String emptyKey = "";
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(emptyKey);
        when(rateLimiter.allowed(emptyKey)).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(keyExtractor).extractKey(request);
        verify(rateLimiter).allowed(emptyKey);
    }

    @Test
    @DisplayName("Should verify correct interaction order for successful request")
    void preHandle_successfulRequest_shouldVerifyInteractionOrder() {
        // Given
        String extractedKey = "test-key";
        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(extractedKey);
        when(rateLimiter.allowed(extractedKey)).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();

        InOrder inOrder = inOrder(rateLimiterProperties, keyExtractor, rateLimiter);
        inOrder.verify(rateLimiterProperties).isEnabled();
        inOrder.verify(keyExtractor).extractKey(request);
        inOrder.verify(rateLimiter).allowed(extractedKey);
    }

    @Test
    @DisplayName("Should handle RuntimeException from rateLimiter.allowed() by propagating it")
    void preHandle_whenRateLimiterThrowsRuntimeException_shouldPropagateException() {
        // Given
        String extractedKey = "test-key";
        RuntimeException runtimeException = new RuntimeException("Rate limiter service unavailable");

        when(rateLimiterProperties.isEnabled()).thenReturn(true);
        when(keyExtractor.extractKey(request)).thenReturn(extractedKey);
        when(rateLimiter.allowed(extractedKey)).thenThrow(runtimeException);

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Rate limiter service unavailable");

        verify(keyExtractor).extractKey(request);
        verify(rateLimiter).allowed(extractedKey);
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle multiple consecutive calls with same key")
        void preHandle_multipleCallsWithSameKey_shouldProcessEachCall() {
            // Given
            String extractedKey = "same-key";
            when(rateLimiterProperties.isEnabled()).thenReturn(true);
            when(keyExtractor.extractKey(any())).thenReturn(extractedKey);
            when(rateLimiter.allowed(extractedKey))
                    .thenReturn(true)  // First call allowed
                    .thenReturn(true)  // Second call allowed
                    .thenReturn(false); // Third call blocked

            // When & Then
            // First call
            boolean result1 = interceptor.preHandle(request, response, handlerMethod);
            assertThat(result1).isTrue();

            // Second call
            boolean result2 = interceptor.preHandle(request, response, handlerMethod);
            assertThat(result2).isTrue();

            // Third call should throw exception
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                    .isInstanceOf(RateLimitExceededException.class);

            verify(keyExtractor, times(3)).extractKey(request);
            verify(rateLimiter, times(3)).allowed(extractedKey);
        }

        @Test
        @DisplayName("Should handle different types of IllegalArgumentException messages")
        void preHandle_differentIllegalArgumentExceptionMessages_shouldHandleAll() {
            // Given
            String[] errorMessages = {
                    "Invalid IP address format",
                    "Missing required headers",
                    "Malformed request URI",
                    "",
                    null
            };

            when(rateLimiterProperties.isEnabled()).thenReturn(true);

            for (String errorMessage : errorMessages) {
                // Given
                reset(keyExtractor); // Reset mock for each iteration
                when(keyExtractor.extractKey(request))
                        .thenThrow(new IllegalArgumentException(errorMessage));

                // When
                boolean result = interceptor.preHandle(request, response, handlerMethod);

                // Then
                assertThat(result).isTrue();
                verify(keyExtractor).extractKey(request);
            }

            verify(rateLimiter, never()).allowed(any());
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should simulate real-world request processing flow")
        void preHandle_realWorldScenario_shouldProcessCorrectly() {
            // Given - Simulating a real HTTP request
            request.setMethod("POST");
            request.setRequestURI("/api/users");
            request.setRemoteAddr("203.0.113.1");
            request.addHeader("User-Agent", "Mozilla/5.0");
            request.addHeader("Authorization", "Bearer token123");

            String expectedKey = "203.0.113.1:/api/users";
            when(rateLimiterProperties.isEnabled()).thenReturn(true);
            when(keyExtractor.extractKey(request)).thenReturn(expectedKey);
            when(rateLimiter.allowed(expectedKey)).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handlerMethod);

            // Then
            assertThat(result).isTrue();
            verify(keyExtractor).extractKey(request);
            verify(rateLimiter).allowed(expectedKey);
        }

        @Test
        @DisplayName("Should handle configuration changes during runtime")
        void preHandle_configurationChanges_shouldAdaptBehavior() {
            // Given - Initially disabled
            when(rateLimiterProperties.isEnabled()).thenReturn(false);

            // When - First call with disabled rate limiter
            boolean result1 = interceptor.preHandle(request, response, handlerMethod);

            // Then
            assertThat(result1).isTrue();
            verify(keyExtractor, never()).extractKey(any());

            // Given - Now enabled
            String key = "dynamic-key";
            when(rateLimiterProperties.isEnabled()).thenReturn(true);
            when(keyExtractor.extractKey(request)).thenReturn(key);
            when(rateLimiter.allowed(key)).thenReturn(true);

            // When - Second call with enabled rate limiter
            boolean result2 = interceptor.preHandle(request, response, handlerMethod);

            // Then
            assertThat(result2).isTrue();
            verify(keyExtractor).extractKey(request);
            verify(rateLimiter).allowed(key);
        }
    }
}