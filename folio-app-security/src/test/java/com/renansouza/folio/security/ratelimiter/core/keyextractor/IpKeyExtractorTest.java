package com.renansouza.folio.security.ratelimiter.core.keyextractor;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("Unit")
class IpKeyExtractorTest {

    private static final String IP = "192.168.0.1";
    private final IpKeyExtractor extractor = new IpKeyExtractor();

    @Test
    void extractKey_ShouldReturnIpFromXForwardedFor_WhenValidIpPresent() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String forwardedFor = IP + ", proxy1, proxy2";
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedFor);

        // Act
        String result = extractor.extractKey(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo("ip:" + IP);
    }

    @Test
    void extractKey_ShouldReturnRemoteAddr_WhenXForwardedForIsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(IP);

        // Act
        String result = extractor.extractKey(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo("ip:" + IP);
    }

    @Test
    void extractKey_ShouldReturnRemoteAddr_WhenXForwardedForDoesNotContainValidIp() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("invalid-ip, proxy");
        when(request.getRemoteAddr()).thenReturn(IP);

        // Act
        String result = extractor.extractKey(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo("ip:" + IP);
    }

    @Test
    void extractKey_ShouldExtractFirstValidIp_WhenMultipleIpsInHeader() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown, " + IP + ", 198.51.100.17");

        // Act
        String result = extractor.extractKey(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo("ip:" + IP);
    }
}