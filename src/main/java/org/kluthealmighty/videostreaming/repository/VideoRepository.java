package org.kluthealmighty.videostreaming.repository;

import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, UUID> {

}
