package com.renansouza.folio.security.ratelimiter.core.keyextractor;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Extracts user-based keys from authenticated HTTP requests for identification or rate limiting
 * purposes.
 * <p>
 * This implementation relies on the servlet container's authentication mechanism to obtain user
 * identity through {@link HttpServletRequest#getUserPrincipal()}. The extracted key is prefixed
 * with "user:" for consistent key formatting.
 * </p>
 *
 * <h3>Authentication Requirements:</h3>
 * <p>
 * This extractor requires that the HTTP request has been authenticated and contains a valid
 * {@link Principal} object. If no authenticated user is found, an exception is thrown.
 * </p>
 *
 * <h3>Key Format:</h3>
 * <p>
 * All returned keys follow the format "user:[USERNAME]" where USERNAME is obtained from
 * {@link Principal#getName()}:
 * </p>
 * <ul>
 *   <li>"user:john.doe"</li>
 *   <li>"user:admin@example.com"</li>
 *   <li>"user:12345"</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // In a secured endpoint with authenticated user
 * KeyExtractor extractor = new UserKeyExtractor();
 * String key = extractor.extractKey(request);
 * // Returns: "user:john.doe"
 *
 * // In an unauthenticated request
 * try {
 *     String key = extractor.extractKey(request);
 * } catch (IllegalArgumentException e) {
 *     // Handle unauthenticated request
 * }
 * }</pre>
 *
 * @author Renan Alberto de Souza
 * @version 1.0.0
 * @see Principal
 * @see HttpServletRequest#getUserPrincipal()
 * @since 1.0.0
 */
public class UserKeyExtractor implements KeyExtractor {

  /**
   * Extracts a user-based key from the authenticated HTTP request.
   * <p>
   * This method retrieves the authenticated user's identity from the request's {@link Principal}
   * object and formats it as a prefixed key string.
   * </p>
   *
   * <h3>Authentication Flow:</h3>
   * <ol>
   *   <li>Retrieves the {@link Principal} from {@link HttpServletRequest#getUserPrincipal()}</li>
   *   <li>If a principal exists, extracts the username using {@link Principal#getName()}</li>
   *   <li>Returns the username prefixed with "user:"</li>
   *   <li>If no principal exists, throws an {@link IllegalArgumentException}</li>
   * </ol>
   *
   * <h3>Security Considerations:</h3>
   * <p>
   * This method assumes that the servlet container's authentication mechanism has
   * properly validated the user's credentials. The quality and format of the username
   * depends on the underlying authentication provider (e.g., database, LDAP, OAuth).
   * </p>
   *
   * @param request the authenticated HTTP servlet request to extract user identity from
   * @return a string key in the format "user:[USERNAME]", never {@code null}
   * @throws IllegalArgumentException if no authenticated user is found in the request (i.e., when
   *                                  {@link HttpServletRequest#getUserPrincipal()} returns
   *                                  {@code null})
   * @throws NullPointerException     if the request parameter is {@code null}
   * @see HttpServletRequest#getUserPrincipal()
   * @see Principal#getName()
   */
  @Override
  public String extractKey(HttpServletRequest request) {
    Principal principal = request.getUserPrincipal();
    if (principal != null) {
      return "user:" + principal.getName();
    }

    throw new IllegalArgumentException("No user identifier found in request");
  }
}