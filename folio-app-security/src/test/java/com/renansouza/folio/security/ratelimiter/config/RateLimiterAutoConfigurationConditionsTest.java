package com.renansouza.folio.security.ratelimiter.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
class RateLimiterAutoConfigurationConditionsTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimiterAutoConfiguration.class));

    @Test
    void shouldCreateBeanWhenEnabled() {
        contextRunner
            .withPropertyValues("rate-limiter.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(WebMvcConfigurer.class);
                assertThat(context).hasSingleBean(HandlerInterceptor.class);
            });
    }

    @Test
    void shouldNotCreateBeanWhenDisabled() {
        contextRunner
            .withPropertyValues("rate-limiter.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(WebMvcConfigurer.class));
    }

    @Test
    void shouldCreateBeanByDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(WebMvcConfigurer.class));
    }

}