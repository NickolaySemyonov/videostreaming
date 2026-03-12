package org.kluthealmighty.videostreaming.service;


import org.kluthealmighty.videostreaming.dto.CreateVideoRequest;
import org.kluthealmighty.videostreaming.dto.UpdateVideoRequest;
import org.kluthealmighty.videostreaming.dto.VideoResponse;
import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.kluthealmighty.videostreaming.exceptions.VideoNotFoundException;
import org.kluthealmighty.videostreaming.exceptions.VideoProcessingException;
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

    @Autowired
    private FileService fileService;

    // ======== API ======== //
    public Flux<VideoResponse> findAllVideo(){
        return videoRepository.findAll()
                .map(this::toDomainVideo);
    }

    public Mono<VideoResponse> findVideoById(UUID id){
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new VideoNotFoundException(id)))
                .map(this::toDomainVideo);
    }


    public Mono<VideoResponse> createVideo(FilePart filePart, CreateVideoRequest request) {
        return fileService.saveFile(filePart)
                .flatMap(filePath ->
                        withCleanupOnError(
                                createVideoEntity(request, filePath).map(this::toDomainVideo),
                                filePath
                        )
                );
    }

    public Mono<VideoResponse> updateVideo(UUID id, FilePart filePart, UpdateVideoRequest request) {
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new VideoNotFoundException(id)))
                .flatMap(existingVideo ->
                        fileService.saveFile(filePart)
                                .flatMap(newFilePath ->
                                        withFileUpdate(
                                                updateVideoEntity(existingVideo, request, newFilePath)
                                                        .map(this::toDomainVideo),
                                                newFilePath,
                                                existingVideo.getPath()
                                        )
                                )
                );
    }

    public Mono<VideoResponse> updateVideoMeta(UUID id, UpdateVideoRequest request){
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new VideoNotFoundException(id)))
                .flatMap(existingVideo -> updateVideoEntity(existingVideo, request, null))
                .map(this::toDomainVideo);
    }

    public Mono<Void> deleteVideo (UUID id){
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new VideoNotFoundException(id)))
                .flatMap(existingVideo -> videoRepository.delete(existingVideo)
                        .then(fileService.deleteFile(existingVideo.getPath()))
                        .onErrorResume(_ -> Mono.error(new VideoProcessingException("Failed to delete video")))
                );
    }






    // ======== HELPERS ======== //

    /**
     * deletes selected file, does not propagate fileService errors
     **/
    private Mono<Void> cleanupFile(String filePath) {
        return fileService.deleteFile(filePath)
                .doOnSuccess(_ -> log.info("Cleaned up: {}", filePath))
                .doOnError(e -> log.error("Failed to cleanup: {}", filePath, e))
                .onErrorResume(_ -> Mono.empty()); //ignore fileService errors
    }


    /**
     * rollback for create operation
     * */
    private <T> Mono<T> withCleanupOnError(Mono<T> source, String filePath) {
        return source.onErrorResume(_ ->
                cleanupFile(filePath).then(Mono.error(new VideoProcessingException("Failed to create video")))
        );
    }


    private <T> Mono<T> withFileUpdate(Mono<T> operation, String newFilePath, String oldFilePath) {
        return operation
                .flatMap(result ->
                        fileService.deleteFile(oldFilePath)
                                .thenReturn(result)
                                .doOnSuccess(_ -> log.info("Old file deleted: {}", oldFilePath))
                                .onErrorResume(deleteError -> {
                                    log.error("Failed to delete old file: {}", oldFilePath, deleteError);
                                    return Mono.just(result);
                                })
                )
                .onErrorResume(error -> {
                    log.error("Operation failed, cleaning up new file: {}", newFilePath, error);
                    return cleanupFile(newFilePath)
                            .then(Mono.error(new VideoProcessingException("Failed to update video")));
                });
    }






    // ======== ENTITY-DTO ======== //


    private Mono<VideoEntity> createVideoEntity(CreateVideoRequest request, String filePath){
        VideoEntity videoEntity = new VideoEntity(
                request.name(),
                request.description(),
                filePath
        );
        return videoRepository.save(videoEntity);
    }


    private Mono<VideoEntity> updateVideoEntity(VideoEntity existingVideo, UpdateVideoRequest request, String newFilePath) {
        VideoEntity updatedVideo = new VideoEntity();
        updatedVideo.setId(existingVideo.getId());
        updatedVideo.setName(
                (request.name() != null && !request.name().isEmpty())
                        ? request.name()
                        : existingVideo.getName()
        );
        updatedVideo.setDescription(
                request.description() != null
                        ? request.description()
                        : existingVideo.getDescription()
        );
        updatedVideo.setPath(
                newFilePath != null
                        ? newFilePath
                        : existingVideo.getPath()
        );
        updatedVideo.setCreatedAt(existingVideo.getCreatedAt());
        return videoRepository.save(updatedVideo);
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
