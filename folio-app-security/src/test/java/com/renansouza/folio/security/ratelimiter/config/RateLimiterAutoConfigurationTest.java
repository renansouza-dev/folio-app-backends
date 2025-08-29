package com.renansouza.folio.security.ratelimiter.config;

import com.renansouza.folio.security.ratelimiter.core.RateLimiter;
import com.renansouza.folio.security.ratelimiter.core.keyextractor.KeyExtractor;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@Tag("Unit")
@ExtendWith(MockitoExtension.class)
class RateLimiterAutoConfigurationTest {

    @Mock
    private HandlerInterceptor mockInterceptor;

    @Mock
    private InterceptorRegistry mockRegistry;

    private RateLimiterAutoConfiguration autoConfiguration;

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(RateLimiterAutoConfiguration.class))
                    .withBean("rateLimiter", RateLimiter.class, () -> mock(RateLimiter.class))
                    .withBean("keyExtractor", KeyExtractor.class, () -> mock(KeyExtractor.class));

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

    @Test
    void shouldCreateBeanWhenEnabled() {
        contextRunner
                .withPropertyValues("rate-limiter.enabled=true")
                .run(context -> {
                    AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(WebMvcConfigurer.class);
                    AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(HandlerInterceptor.class);
                    AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(RateLimiter.class);
                    AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(KeyExtractor.class);
                    AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(RateLimiterProperties.class);
                });
    }

    @Test
    void shouldNotCreateBeanWhenDisabled() {
        contextRunner
                .withPropertyValues("rate-limiter.enabled=false")
                .run(context -> AssertionsForInterfaceTypes.assertThat(context).doesNotHaveBean(WebMvcConfigurer.class));
    }

    @Test
    void shouldCreateBeanByDefault() {
        contextRunner.run(context -> AssertionsForInterfaceTypes.assertThat(context).hasSingleBean(WebMvcConfigurer.class));
    }

}