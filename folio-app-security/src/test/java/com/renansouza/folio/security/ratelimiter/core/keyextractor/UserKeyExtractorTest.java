package com.renansouza.folio.security.ratelimiter.core.keyextractor;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("Unit")
class UserKeyExtractorTest {

    private final UserKeyExtractor extractor = new UserKeyExtractor();

    @Test
    void extractKey_ShouldReturnUserKey_WhenPrincipalIsPresent() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Principal principal = mock(Principal.class);

        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("me");

        // Act
        String result = extractor.extractKey(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo("user:me");
    }

    @Test
    void extractKey_ShouldThrowException_WhenPrincipalIsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> extractor.extractKey(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No user identifier found in request");
    }

}