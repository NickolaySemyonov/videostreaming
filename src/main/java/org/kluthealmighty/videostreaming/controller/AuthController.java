package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.UserCredentials;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> registerUser(@RequestBody UserCredentials credentials){
        return userService.createUser(credentials)
                .map(userResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(userResponse)
                );
    }

}

