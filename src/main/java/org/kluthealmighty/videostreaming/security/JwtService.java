package org.kluthealmighty.videostreaming.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

    @Autowired
    private JwtUtil jwtUtil;

    public void setAccessTokenCookie(ServerHttpResponse response, JwtPrincipal principal) {
        String token = jwtUtil.generateAccessToken(principal);
        Long maxAgeSeconds = jwtUtil.getTokenExpirationSeconds(token);
        ResponseCookie tokenCookie = createTokenCookie("ACCESS_TOKEN", token, maxAgeSeconds);
        response.addCookie(tokenCookie);
    }

    public void setRefreshTokenCookie(ServerHttpResponse response, JwtPrincipal principal) {
        String token = jwtUtil.generateRefreshToken(principal);
        Long maxAgeSeconds = jwtUtil.getTokenExpirationSeconds(token);
        ResponseCookie tokenCookie = createTokenCookie("REFRESH_TOKEN", token, maxAgeSeconds);
        response.addCookie(tokenCookie);
    }

    public void revokeTokenCookies(ServerHttpResponse response) {
        ResponseCookie accessToken = createTokenCookie("ACCESS_TOKEN", "", 0L);
        ResponseCookie refreshToken = createTokenCookie("REFRESH_TOKEN","", 0L);

        response.addCookie(accessToken);
        response.addCookie(refreshToken);
    }

    private ResponseCookie createTokenCookie(String tokenType, String token, Long maxAgeSeconds){
        return ResponseCookie.from(tokenType, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")  // Strict, Lax, or None
                .build();
    }

    public Mono<Void> refresh(ServerWebExchange exchange) {
        String refreshToken = extractTokenFromCookie(exchange.getRequest(), "REFRESH_TOKEN");

        return jwtUtil.validateToken(refreshToken)
                .flatMap(claims -> {
                    JwtPrincipal principal = jwtUtil.toPrincipal(claims);
                    setRefreshTokenCookie(exchange.getResponse(), principal);
                    setAccessTokenCookie(exchange.getResponse(),  principal);
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    revokeTokenCookies(exchange.getResponse());
                    return Mono.error(new Exception("Token refresh failed", e));
                })
                .then();
    }

    public String extractTokenFromCookie(ServerHttpRequest request, String cookieName) {
        var tokenCookie = request.getCookies().getFirst(cookieName);
        return (tokenCookie==null ? null : tokenCookie.getValue());
    }
}
