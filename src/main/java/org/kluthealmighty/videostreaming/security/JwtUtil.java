package org.kluthealmighty.videostreaming.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);


    public String generateAccessToken(JwtPrincipal principal) {
        return generateToken(principal.userId(),principal.email(), principal.channelTag(), ACCESS_TOKEN_DURATION);
    }

    public String generateRefreshToken(JwtPrincipal principal) {
        return generateToken(principal.userId(),principal.email(), principal.channelTag(),  REFRESH_TOKEN_DURATION);
    }

    private String generateToken(Long userId, String username, String channelTag, Duration duration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("channelTag", channelTag);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration.toMillis()))
                .and()
                .signWith(getKey())
                .compact();
    }

    public Long getTokenExpirationSeconds(String token) {
        Date expiration = extractExpiration(token);
        long now = System.currentTimeMillis();
        long secondsUntilExpiration = (expiration.getTime() - now) / 1000;

        return Math.max(secondsUntilExpiration, 0);
    }

    public Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = extractAllClaims(token);
                if (isTokenExpired(claims.getExpiration()))
                    throw new Exception("Invalid JWT token: token expired");

                return claims;
            } catch (JwtException e) {
                throw new Exception("Invalid JWT token: " + e.getMessage());
            }
        });
    }

    public JwtPrincipal toPrincipal(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        String channelTag = claims.get("channelTag", String.class);
        return new JwtPrincipal(userId, email, channelTag);
    }


//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }

//    private Long extractUserId(String token){
//        return extractClaim(token, claims -> claims.get("userId", Long.class));
//    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private boolean isTokenExpired(Date expirationDate) {
        return expirationDate.before(new Date());
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
