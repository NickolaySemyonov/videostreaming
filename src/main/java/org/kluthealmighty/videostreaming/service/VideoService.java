package org.kluthealmighty.videostreaming.service;

import jakarta.persistence.EntityNotFoundException;
import org.kluthealmighty.videostreaming.DTO.VideoDTO;
import org.kluthealmighty.videostreaming.DTO.VideoUpdateDTO;
import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.kluthealmighty.videostreaming.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public List<VideoDTO> findAllVideo(){
        List<VideoEntity> videoEntities = videoRepository.findAll();

        return videoEntities.stream()
                .map(this::toDomainVideo)
                .toList();
    }

    public VideoDTO findVideoById(UUID id){
        VideoEntity videoEntity = videoRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Not found video with id: " + id));

        return toDomainVideo(videoEntity);
    }


    public VideoDTO createVideo(VideoDTO videoToCreate){
        if (videoToCreate.id() != null){
            throw new IllegalArgumentException("id should be empty");
        }
        var entityToSave = new VideoEntity(
                null,
                videoToCreate.name(),
                videoToCreate.description(),
                null
        );
        var createdVideo = videoRepository.save(entityToSave);
        return toDomainVideo(createdVideo);
    }

    public VideoDTO updateVideo(UUID id, VideoUpdateDTO videoToUpdate){
        var existingVideo = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));

        if (videoToUpdate.name() != null) {
            existingVideo.setName(videoToUpdate.name());
        }
        if (videoToUpdate.description() != null) {
            existingVideo.setDescription(videoToUpdate.description());
        }

        var updatedVideo = videoRepository.save(existingVideo);
        return toDomainVideo(updatedVideo);
    }

    public void deleteVideo(UUID id){
        var existingVideo = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
        videoRepository.delete(existingVideo);
    }


    private VideoDTO toDomainVideo(VideoEntity video){
        return new VideoDTO(
                video.getId(),
                video.getName(),
                video.getDescription(),
                video.getCreatedAt()
        );
    }

}
