package io.finarkein.fiul.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.PathContainer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.util.pattern.PathPattern;

import java.util.List;

import static io.finarkein.fiul.config.NotificationConfig.NOTIFICATION_API_PATTERNS;

@Configuration
public class NotificationSecurityConfig {

    @Bean
    protected SecurityWebFilterChain configure(ServerHttpSecurity http,
                                               @Qualifier(NOTIFICATION_API_PATTERNS) List<PathPattern> pathPatterns,
                                               @Value("${aa.common.token.issuer}") String tokenIssuer,
                                               @Value("${aa.common.token.header}") String tokenHeaderName) {
        http.securityMatcher(exchange -> {
                    PathContainer pathContainer = exchange.getRequest().getPath().pathWithinApplication();
                    for (PathPattern pattern : pathPatterns) {
                        if (pattern.matches(pathContainer)) { // if any path matches
                            return ServerWebExchangeMatcher.MatchResult.match();
                        }
                    }
                    return ServerWebExchangeMatcher.MatchResult.notMatch();
                })
                .csrf().disable()
                .httpBasic().disable()
                .cors().disable()
                .authorizeExchange()
                // .anyExchange().hasRole("ROLES_AA") TODO: fix check for ROLES
                .and()
                .oauth2ResourceServer(oauth2 -> {
                    // AA
                    Oauth2TokenConverter tokenConverter = new Oauth2TokenConverter();
                    tokenConverter.setTokenPattern(Oauth2TokenConverter.AA_PATTERN);
                    tokenConverter.setTokenHeaderName(tokenHeaderName);
                    oauth2.bearerTokenConverter(tokenConverter);
                    oauth2.authenticationManagerResolver(new JwtIssuerReactiveAuthenticationManagerResolver(tokenIssuer));
                });

        return http.build();
    }
}
