package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.dto.UserCredentials;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.entity.UserEntity;
import org.kluthealmighty.videostreaming.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }


    public Mono<UserResponse> createUser(UserCredentials request){
        return userRepository.existsByEmail(request.email())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new RuntimeException("User already exists with email: " + request.email())))
                .flatMap(_ -> createUserEntity(request))
                .map(this::toDomainUser);
    }

    private Mono<UserEntity> createUserEntity(UserCredentials request){
        String encryptedPassword = passwordEncoder.encode(request.password());
        UserEntity userEntity = new UserEntity(
                null,
                request.email(),
                encryptedPassword
        );
        return userRepository.save(userEntity);
    }

    private UserResponse toDomainUser(UserEntity userEntity){
        return new UserResponse(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getPasswordHash()
        );
    }

//    public Mono<String> verify(UserCredentials credentials) {
//        UsernamePasswordAuthenticationToken token =
//                new UsernamePasswordAuthenticationToken(
//                        credentials.email(),
//                        credentials.password()
//                );
//
//        return authenticationManager.authenticate(token)
//                .map(auth -> {
//                    if (auth.isAuthenticated()) {
//                        return jwtService.generateToken(credentials.email());
//                    } else {
//                        return "Fail";
//                    }
//                })
//                .onErrorReturn("Fail"); // Handle authentication errors
//    }


}
