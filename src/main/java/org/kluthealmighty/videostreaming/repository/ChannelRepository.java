package org.kluthealmighty.videostreaming.repository;

import org.kluthealmighty.videostreaming.entity.ChannelEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChannelRepository extends ReactiveCrudRepository<ChannelEntity, Long> {
}
