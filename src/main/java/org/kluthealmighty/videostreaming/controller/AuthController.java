package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.AuthRequest;
import org.kluthealmighty.videostreaming.security.JwtService;
import org.kluthealmighty.videostreaming.security.UserPrincipal;
import org.kluthealmighty.videostreaming.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
    public Mono<ResponseEntity<String>> registerUser(
            @RequestBody AuthRequest credentials,
            ServerWebExchange exchange
    ) {
        return userService.createUser(credentials)
                .map(user -> {
                    jwtService.setAccessTokenCookie(
                            exchange.getResponse(),
                            user.email(),
                            user.id()
                    );
                    jwtService.setRefreshTokenCookie(
                            exchange.getResponse(),
                            user.email(),
                            user.id()
                    );
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body("Registered and authenticated");
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(
            @RequestBody AuthRequest request,
            ServerWebExchange exchange
    ) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        );
        return authenticationManager.authenticate(auth)
                .map(authenticated -> {
                    UserPrincipal principal = (UserPrincipal) authenticated.getPrincipal();

                    if (principal==null)
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

                    jwtService.setAccessTokenCookie(
                            exchange.getResponse(),
                            principal.getUsername(),
                            principal.getUserId()
                    );
                    jwtService.setRefreshTokenCookie(
                            exchange.getResponse(),
                            principal.getUsername(),
                            principal.getUserId()
                    );
                    return ResponseEntity.ok("Logged in");
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout (ServerWebExchange exchange){
        jwtService.revokeTokenCookies(exchange.getResponse());
        return Mono.just(ResponseEntity.noContent().build());
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Void>> refresh (ServerWebExchange exchange){
        return jwtService.refresh(exchange)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

