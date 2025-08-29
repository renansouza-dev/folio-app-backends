package com.renansouza.folio.security.ratelimiter.core.keyextractor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts IP address-based keys from HTTP requests for identification or rate limiting purposes.
 * <p>
 * This implementation prioritizes the IP address from the "X-Forwarded-For" header when available,
 * falling back to the direct remote address. The extracted key is prefixed with "ip:" for
 * consistent key formatting.
 * </p>
 *
 * <h3>IP Address Resolution Priority:</h3>
 * <ol>
 *   <li>First valid IP address found in the "X-Forwarded-For" header</li>
 *   <li>Direct remote address from the request ({@link HttpServletRequest#getRemoteAddr()})</li>
 * </ol>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * KeyExtractor extractor = new IpKeyExtractor();
 * String key = extractor.extractKey(request);
 * // Returns: "ip:192.168.1.100" or "ip:203.0.113.45"
 * }</pre>
 *
 * @author Renan Alberto de Souza
 * @version 1.0.0
 * @since 1.0.0
 */
public class IpKeyExtractor implements KeyExtractor {

    /**
     * Regular expression pattern for matching IPv4 addresses.
     * <p>
     * Matches valid IPv4 addresses with octets ranging from 0-255:
     * </p>
     * <ul>
     *   <li>25[0-5]: Matches 250-255</li>
     *   <li>2[0-4]\\d: Matches 200-249</li>
     *   <li>1\\d\\d: Matches 100-199</li>
     *   <li>[1-9]?\\d: Matches 0-99</li>
     * </ul>
     */
    private static final String IP_REGEX = "\\b(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}\\b";

    /**
     * Compiled pattern for efficient IP address matching with multiline support.
     */
    private static final Pattern PATTERN = Pattern.compile(IP_REGEX, Pattern.MULTILINE);

    /**
     * Extracts an IP-based key from the HTTP request.
     * <p>
     * The method follows this resolution order:
     * </p>
     * <ol>
     *   <li>Checks the "X-Forwarded-For" header for the first valid IP address</li>
     *   <li>Falls back to {@link HttpServletRequest#getRemoteAddr()} if no valid IP is found in the header</li>
     * </ol>
     *
     * <h3>X-Forwarded-For Header Handling:</h3>
     * <p>
     * The "X-Forwarded-For" header typically contains a comma-separated list of IP addresses
     * when requests pass through proxies or load balancers. This method extracts the first
     * valid IP address found in the header value using regex pattern matching.
     * </p>
     *
     * <h3>Key Format:</h3>
     * <p>
     * All returned keys are prefixed with "ip:" for consistent identification:
     * </p>
     * <ul>
     *   <li>"ip:192.168.1.100"</li>
     *   <li>"ip:203.0.113.45"</li>
     *   <li>"ip:10.0.0.1"</li>
     * </ul>
     *
     * @param request the HTTP servlet request to extract the IP address from
     * @return a string key in the format "ip:[IP_ADDRESS]", never {@code null}
     * @throws NullPointerException if the request parameter is {@code null}
     *
     * @see HttpServletRequest#getHeader(String)
     * @see HttpServletRequest#getRemoteAddr()
     */
    @Override
    public String extractKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null) {
            Matcher matcher = PATTERN.matcher(ip);
            if (matcher.find()) {
                return "ip:" + matcher.group(0);
            }
        }

        return "ip:" + request.getRemoteAddr();
    }
}