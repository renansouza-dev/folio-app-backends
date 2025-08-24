package com.renansouza.folio.security.ratelimiter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RateLimiterAutoConfigurationTest {

    @Mock
    private HandlerInterceptor mockInterceptor;

    @Mock
    private InterceptorRegistry mockRegistry;

    private RateLimiterAutoConfiguration autoConfiguration;

    @BeforeEach
    void setUp() {
        autoConfiguration = new RateLimiterAutoConfiguration();
    }

    @Test
    void webMvcConfigurer_shouldAddInterceptor() {
        // Given
        WebMvcConfigurer configurer = autoConfiguration.webMvcConfigurer(mockInterceptor);

        // When
        configurer.addInterceptors(mockRegistry);

        // Then
        verify(mockRegistry).addInterceptor(mockInterceptor);
        verifyNoMoreInteractions(mockRegistry);
    }

    @Test
    void webMvcConfigurer_shouldNotBeNull() {
        // When
        WebMvcConfigurer configurer = autoConfiguration.webMvcConfigurer(mockInterceptor);

        // Then
        assertThat(configurer).isNotNull();
    }

}