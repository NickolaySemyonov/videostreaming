package org.kluthealmighty.videostreaming.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtService jwtService;

    private static final String ACCESS_TOKEN_NAME = "ACCESS_TOKEN";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = jwtService.extractTokenFromCookie(exchange.getRequest(), ACCESS_TOKEN_NAME);

        if (token == null) {
            // No token present, continue without authentication
            return chain.filter(exchange);
        }

        // Validate token and create authentication
        return jwtUtil.validateToken(token)
                .map(claims -> jwtUtil.toPrincipal(claims))
                .map(this::createAuthentication)

                // Continue filter chain with security context populated
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                // If token validation fails, continue without authentication
                .onErrorResume(_ -> chain.filter(exchange));
    }

    private Authentication createAuthentication(JwtPrincipal principal) {
        // Create authentication with authorities/permissions if needed
        //Collection<GrantedAuthority> authorities = extractAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null, // credentials (token) - can be null or the token itself
                null // authorities
        );
    }
}
