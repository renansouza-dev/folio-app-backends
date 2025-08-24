package com.renansouza.folio.security.ratelimiter.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RateLimiterPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(RateLimiterProperties.class)
    static class TestConfig { }

    @Test
    void whenPropertiesProvided_thenBindSuccessfully() {
        contextRunner
                .withPropertyValues(
                        "rate-limiter.enabled=true",
                        "rate-limiter.key=ip",
                        "rate-limiter.algorithm=fixed_window",
                        "rate-limiter.storage=in_memory",
                        "rate-limiter.rules[0].path=/api/orders",
                        "rate-limiter.rules[0].method=GET",
                        "rate-limiter.rules[0].limit=10",
                        "rate-limiter.rules[0].duration=60",
                        "rate-limiter.rules[1].path=/api/payments",
                        "rate-limiter.rules[1].method=POST",
                        "rate-limiter.rules[1].limit=5",
                        "rate-limiter.rules[1].duration=120"
                )
                .run(context -> {
                    RateLimiterProperties props = context.getBean(RateLimiterProperties.class);

                    assertThat(props.isEnabled()).isTrue();
                    assertThat(props.getKey()).isEqualTo(RateLimiterProperties.KeyType.IP);
                    assertThat(props.getAlgorithm()).isEqualTo(RateLimiterProperties.AlgorithmType.FIXED_WINDOW);
                    assertThat(props.getStorage()).isEqualTo(RateLimiterProperties.StorageType.IN_MEMORY);

                    Set<RateLimiterProperties.Rule> rules = props.getRules();
                    assertThat(rules).hasSize(2);

                    RateLimiterProperties.Rule ordersRule = rules.stream()
                            .filter(r -> r.getPath().equals("/api/orders"))
                            .findFirst().orElseThrow();
                    assertThat(ordersRule.getMethod()).isEqualTo(HttpMethod.GET.name());
                    assertThat(ordersRule.getLimit()).isEqualTo(10);
                    assertThat(ordersRule.getDuration()).isEqualTo(60);

                    RateLimiterProperties.Rule paymentsRule = rules.stream()
                            .filter(r -> r.getPath().equals("/api/payments"))
                            .findFirst().orElseThrow();
                    assertThat(paymentsRule.getMethod()).isEqualTo(HttpMethod.POST.name());
                    assertThat(paymentsRule.getLimit()).isEqualTo(5);
                    assertThat(paymentsRule.getDuration()).isEqualTo(120);
                });
    }

    @Test
    void whenNoPropertiesProvided_thenDefaultsApply() {
        contextRunner.run(context -> {
            RateLimiterProperties props = context.getBean(RateLimiterProperties.class);

            assertAll(
                    () -> assertThat(props.isEnabled()).isFalse(),
                    () -> assertThat(props.getKey()).isNull(),
                    () -> assertThat(props.getAlgorithm()).isNull(),
                    () -> assertThat(props.getStorage()).isNull(),
                    () -> assertThat(props.getRules()).isNotNull().isEmpty()
            );
        });
    }

}