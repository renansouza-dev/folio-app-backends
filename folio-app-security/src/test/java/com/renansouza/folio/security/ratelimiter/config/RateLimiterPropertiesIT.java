package com.renansouza.folio.security.ratelimiter.config;

import com.renansouza.folio.security.ratelimiter.controller.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
        "rate-limiter.enabled=true",
        "rate-limiter.key=IP",
        "rate-limiter.algorithm=TOKEN_BUCKET",
        "rate-limiter.storage=REDIS",
        "rate-limiter.rules[0].path=/api/test",
        "rate-limiter.rules[0].method=GET",
        "rate-limiter.rules[0].limit=10",
        "rate-limiter.rules[0].duration=60",
        "rate-limiter.rules[1].path=/api/admin",
        "rate-limiter.rules[1].method=POST",
        "rate-limiter.rules[1].limit=5",
        "rate-limiter.rules[1].duration=120"
})
class RateLimiterPropertiesIT {

    @Autowired
    private RateLimiterProperties properties;

    @Test
    void shouldBindTopLevelProperties() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getKey()).isEqualTo(RateLimiterProperties.KeyType.IP);
        assertThat(properties.getAlgorithm()).isEqualTo(RateLimiterProperties.AlgorithmType.TOKEN_BUCKET);
        assertThat(properties.getStorage()).isEqualTo(RateLimiterProperties.StorageType.REDIS);
    }

    @Test
    void shouldBindRuleListProperties() {
        assertThat(properties.getRules()).hasSize(2);

        RateLimiterProperties.Rule rule1 = properties.getRules().stream()
                .filter(r -> r.getPath().equals("/api/test"))
                .findFirst()
                .orElseThrow();

        assertThat(rule1.getMethod()).isEqualTo("GET");
        assertThat(rule1.getLimit()).isEqualTo(10);
        assertThat(rule1.getDuration()).isEqualTo(60);

        RateLimiterProperties.Rule rule2 = properties.getRules().stream()
                .filter(r -> r.getPath().equals("/api/admin"))
                .findFirst()
                .orElseThrow();

        assertThat(rule2.getMethod()).isEqualTo("POST");
        assertThat(rule2.getLimit()).isEqualTo(5);
        assertThat(rule2.getDuration()).isEqualTo(120);
    }

}