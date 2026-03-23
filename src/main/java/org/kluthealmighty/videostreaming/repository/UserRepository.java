package org.kluthealmighty.videostreaming.repository;

import org.kluthealmighty.videostreaming.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {
    Mono<UserEntity> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<UserEntity> findByChannelTag(String channelTag);
    Mono<Boolean> existsByChannelTag(String channelTag);
}
