package com.renansouza.folio.security.ratelimiter.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RateLimiterAutoConfiguration.class)
@TestPropertySource(properties = "rate-limiter.enabled=true")
class RateLimiterAutoConfigurationIT {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldCreateWebMvcConfigurer() {
        // Then
        assertThat(context.getBeansOfType(WebMvcConfigurer.class))
            .hasSize(1)
            .containsKey("webMvcConfigurer");
    }

    @Test
    void shouldCreateHandlerInterceptor() {
        // Then
        assertThat(context.getBeansOfType(HandlerInterceptor.class))
            .hasSize(1);
    }

    @Test
    void shouldBindRateLimiterProperties() {
        RateLimiterProperties props = context.getBean(RateLimiterProperties.class);

        assertThat(props).isNotNull();
        assertThat(props.isEnabled()).isTrue();
    }
}