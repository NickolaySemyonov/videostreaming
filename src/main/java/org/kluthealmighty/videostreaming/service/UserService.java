package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.dto.AuthRequest;
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

    public Mono<UserResponse> findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .map(this::toDomainUser);
    }


    public Mono<UserResponse> createUser(AuthRequest request){
        return userRepository.existsByEmail(request.email())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new RuntimeException("User already exists with email: " + request.email())))
                .flatMap(_ -> createUserEntity(request))
                .map(this::toDomainUser);
    }

    private Mono<UserEntity> createUserEntity(AuthRequest request){
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

}
