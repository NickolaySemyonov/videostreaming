package org.kluthealmighty.videostreaming.service;


import org.kluthealmighty.videostreaming.DTO.CreateVideoRequest;
import org.kluthealmighty.videostreaming.DTO.UpdateVideoRequest;
import org.kluthealmighty.videostreaming.DTO.VideoResponse;
import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.kluthealmighty.videostreaming.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static reactor.netty.http.HttpConnectionLiveness.log;


@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired FileService fileService;


    public Flux<VideoResponse> findAllVideo(){
        return videoRepository
                .findAll()
                .map(this::toDomainVideo);
    }

    public Mono<VideoResponse> findVideoById(UUID id){
        return videoRepository.findById(id).map(this::toDomainVideo);
    }


    public Mono<VideoResponse> createVideo(FilePart filePart, CreateVideoRequest videoToCreate) {
        return  fileService.saveFile(filePart)
                .flatMap(videoPath -> {
                    VideoEntity videoEntity = new VideoEntity(
                            videoToCreate.name(),
                            videoToCreate.description(),
                            videoPath
                    );
                    return videoRepository.save(videoEntity);
                })
                .map(this::toDomainVideo);
    }


    // UPDATE - Full update with file
    public Mono<VideoResponse> updateVideo(UUID id, FilePart filePart, CreateVideoRequest request) {
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new Exception("Video not found: " + id)))
                .flatMap(existing -> {
                    String oldPath = existing.getPath();

                    return fileService.updateFile(oldPath, filePart)
                            .flatMap(newPath -> {
                                existing.setName(request.name());
                                existing.setDescription(request.description());
                                existing.setPath(newPath);
                                return videoRepository.save(existing);
                            })
                            .flatMap(savedVideo ->
                                    fileService.clearBackup(oldPath)
                                            .thenReturn(savedVideo)
                            );
                })
                .map(this::toDomainVideo)
                .doOnSuccess(v -> {
                    assert v != null;
                    log.info("Updated video: {}", v.id());
                });
    }

    // UPDATE - Metadata only (no file change)
    public Mono<VideoResponse> updateVideoMetadata(UUID id, UpdateVideoRequest request) {
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new Exception("Video not found: " + id)))
                .flatMap(existing -> {
                    if (request.name() != null) {
                        existing.setName(request.name());
                    }
                    if (request.description() != null) {
                        existing.setDescription(request.description());
                    }
                    return videoRepository.save(existing);
                })
                .map(this::toDomainVideo)
                .doOnSuccess(v -> {
                    assert v != null;
                    log.info("Updated video metadata: {}", v.id());
                });
    }


    public Mono<Void> deleteVideo(UUID id){
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new Exception("Video not found: " + id)))
                .flatMap(videoEntity -> {
                    String originalPath = videoEntity.getPath();

                    return fileService.deleteFile(originalPath)
                            .flatMap(backupPath ->
                                    videoRepository.delete(videoEntity)
                                            .then(fileService.clearBackup(backupPath))
                            );
                });

    }


    private VideoResponse toDomainVideo(VideoEntity video){
        return new VideoResponse(
                video.getId(),
                video.getName(),
                video.getDescription(),
                video.getPath(),
                video.getCreatedAt()
        );
    }

}
