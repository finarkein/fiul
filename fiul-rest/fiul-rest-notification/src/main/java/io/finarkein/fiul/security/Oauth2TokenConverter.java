package io.finarkein.fiul.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Oauth2TokenConverter implements ServerAuthenticationConverter {

    private static final String BASE_PATTERN = "(?<token>[a-zA-Z0-9-._~+/]+=*)$";
    public static final Pattern AA_PATTERN = Pattern.compile("^" + BASE_PATTERN, Pattern.CASE_INSENSITIVE);
    public static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer " + BASE_PATTERN, Pattern.CASE_INSENSITIVE);

    private boolean allowUriQueryParameter = false;

    private String tokenHeaderName = HttpHeaders.AUTHORIZATION;
    private Pattern tokenPattern = AA_PATTERN;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> token(exchange.getRequest())).map(token -> {
            if (token.isEmpty()) {
                BearerTokenError error = invalidTokenError();
                throw new OAuth2AuthenticationException(error);
            }
            return new BearerTokenAuthenticationToken(token);
        });
    }

    private String token(ServerHttpRequest request) {
        String authorizationHeaderToken = resolveFromAuthorizationHeader(request.getHeaders());
        String parameterToken = request.getQueryParams().getFirst("access_token");
        if (authorizationHeaderToken != null) {
            if (parameterToken != null) {
                BearerTokenError error = BearerTokenErrors
                        .invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }
            return authorizationHeaderToken;
        }
        if (parameterToken != null && isParameterTokenSupportedForRequest(request)) {
            return parameterToken;
        }
        return null;
    }

    /**
     * Set if transport of access token using URI query parameter is supported. Defaults
     * to {@code false}.
     * <p>
     * The spec recommends against using this mechanism for sending bearer tokens, and
     * even goes as far as stating that it was only included for completeness.
     *
     * @param allowUriQueryParameter if the URI query parameter is supported
     */
    public void setAllowUriQueryParameter(boolean allowUriQueryParameter) {
        this.allowUriQueryParameter = allowUriQueryParameter;
    }

    /**
     * Set this value to configure what header is checked when resolving a Bearer Token.
     * This value is defaulted to {@link HttpHeaders#AUTHORIZATION}.
     * <p>
     * This allows other headers to be used as the Bearer Token source such as
     * {@link HttpHeaders#PROXY_AUTHORIZATION}
     *
     * @param tokenHeaderName the header to check when retrieving the Bearer Token.
     * @since 5.4
     */
    public void setTokenHeaderName(String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    public void setTokenPattern(String pattern) {
        this.tokenPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    public void setTokenPattern(Pattern pattern) {
        this.tokenPattern = pattern;
    }

    private String resolveFromAuthorizationHeader(HttpHeaders headers) {
        String authorization = headers.getFirst(this.tokenHeaderName);
        if (Objects.isNull(authorization)) {
            return null;
        }
        Matcher matcher = tokenPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = invalidTokenError();
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

    private static BearerTokenError invalidTokenError() {
        return BearerTokenErrors.invalidToken("Token is malformed");
    }

    private boolean isParameterTokenSupportedForRequest(ServerHttpRequest request) {
        return this.allowUriQueryParameter && HttpMethod.GET.equals(request.getMethod());
    }

}