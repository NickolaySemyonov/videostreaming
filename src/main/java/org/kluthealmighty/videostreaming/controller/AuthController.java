package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.AuthRequest;
import org.kluthealmighty.videostreaming.dto.AuthResponse;
import org.kluthealmighty.videostreaming.security.JwtPrincipal;
import org.kluthealmighty.videostreaming.security.JwtService;
import org.kluthealmighty.videostreaming.security.UserPrincipal;
import org.kluthealmighty.videostreaming.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(
            @RequestBody AuthRequest request,
            ServerWebExchange exchange
    ) {
        return userService.createUser(request)
                .map(user -> {
                    JwtPrincipal principal = new JwtPrincipal(user.id(), user.email(), user.channelTag());

                    jwtService.setAccessTokenCookie(exchange.getResponse(), principal);
                    jwtService.setRefreshTokenCookie(exchange.getResponse(), principal);
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new AuthResponse(principal.channelTag(), "Registration successful") );
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(
            @RequestBody AuthRequest request,
            ServerWebExchange exchange
    ) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        );
        return authenticationManager.authenticate(auth)
                .map(authenticated -> {
                    UserPrincipal userPrincipal = (UserPrincipal) authenticated.getPrincipal();

                    if (userPrincipal == null)
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

                    //convert to principal not containing password
                    JwtPrincipal principal = new JwtPrincipal(
                            userPrincipal.getUserId(),
                            userPrincipal.getUsername(),
                            userPrincipal.getChannelTag()
                    );
                    jwtService.setAccessTokenCookie(exchange.getResponse(), principal);
                    jwtService.setRefreshTokenCookie(exchange.getResponse(), principal);
                    return ResponseEntity.ok(new AuthResponse(principal.channelTag(), "Logged in"));
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        jwtService.revokeTokenCookies(exchange.getResponse());
        return Mono.just(ResponseEntity.noContent().build());
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Void>> refresh(ServerWebExchange exchange) {
        return jwtService.refresh(exchange)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

