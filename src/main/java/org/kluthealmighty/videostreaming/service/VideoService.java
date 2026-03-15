package org.kluthealmighty.videostreaming.service;


import org.kluthealmighty.videostreaming.dto.CreateVideoRequest;
import org.kluthealmighty.videostreaming.dto.UpdateVideoRequest;
import org.kluthealmighty.videostreaming.dto.VideoResponse;
import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.kluthealmighty.videostreaming.enums.FilePartType;
import org.kluthealmighty.videostreaming.exceptions.VideoNotFoundException;
import org.kluthealmighty.videostreaming.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static reactor.netty.http.HttpConnectionLiveness.log;


@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private FileService fileService;


    // ======== API ======== //
    public Flux<VideoResponse> findAllVideos(){
        return videoRepository.findAll()
                .map(this::toDomainVideo);
    }

    public Mono<VideoResponse> findVideoById(UUID id){
        return videoRepository.findById(id)
                .switchIfEmpty(Mono.error(new VideoNotFoundException(id)))
                .map(this::toDomainVideo);
    }

    public Mono<VideoResponse> createVideo(CreateVideoRequest request, FilePart thumbnailPart, FilePart videoPart) {
        return Mono.usingWhen(
                //Context
                Mono.just(new CreateContext()),
                //Action
                ctx -> fileService.saveFile(thumbnailPart, FilePartType.THUMBNAIL)
                        .flatMap(thumbnailPath -> {
                            ctx.thumbnailPath = thumbnailPath;
                            return fileService.saveFile(videoPart, FilePartType.VIDEO);
                        })
                        .flatMap(videoPath -> {
                            ctx.videoPath = videoPath;
                            return createVideoEntity(request, ctx.thumbnailPath, ctx.videoPath);
                        })
                        .map(this::toDomainVideo)
                        .doOnSuccess(_ -> log.info("Successfully created video")),
                //onSuccess
                ctx -> ctx.cleanup(fileService),
                //onError
                (ctx, _) -> ctx.rollback(fileService),
                //onCancel
                ctx -> ctx.rollback(fileService)
        );
    }

    public Mono<VideoResponse> updateVideo(UUID videoId, UpdateVideoRequest request, FilePart thumbnailPart){
        Mono<FilePart> thumbnailFilePartMono = Mono.justOrEmpty(thumbnailPart);
        return Mono.usingWhen(
                //Context
                Mono.just(new UpdateContext()),
                //Action
                ctx -> videoRepository.findById(videoId)
                        .switchIfEmpty(Mono.error(new VideoNotFoundException(videoId)))
                        .flatMap(videoEntity -> {
                            ctx.existingVideoEntity = videoEntity;
                            ctx.oldThumbnailPath = videoEntity.getThumbnailPath();

                            return thumbnailFilePartMono
                                    .flatMap(filePart -> fileService.saveFile(filePart, FilePartType.THUMBNAIL))
                                    .defaultIfEmpty(videoEntity.getThumbnailPath());
                        })
                        .flatMap(newThumbnailPath -> {
                            ctx.newThumbnailPath = newThumbnailPath;
                            return updateVideoEntity(ctx.existingVideoEntity, request, ctx.newThumbnailPath);
                        })
                        .map(this::toDomainVideo)
                        .doOnSuccess(_ -> log.info("Successfully updated video with id: " + videoId)),
                //onSuccess
                ctx -> ctx.cleanup(fileService),
                //onError
                (ctx, _) -> ctx.rollback(fileService),
                //onCancel
                ctx -> ctx.cleanup(fileService)
        );
    }

    public Mono<Void> deleteVideo(UUID videoId){
        return Mono.usingWhen(
                //Context
                Mono.just(new DeleteContext()),
                //Action
                ctx -> videoRepository.findById(videoId)
                        .switchIfEmpty(Mono.error(new VideoNotFoundException(videoId)))
                        .flatMap(videoEntity -> {
                            ctx.thumbnailPath = videoEntity.getThumbnailPath();
                            ctx.videoPath = videoEntity.getVideoPath();
                            return videoRepository.delete(videoEntity);
                        })
                        .doOnSuccess(_ -> log.info("Successfully deleted video with id: " + videoId)),
                //onSuccess
                ctx -> ctx.cleanup(fileService),
                //onError
                (ctx, _) -> ctx.rollback(fileService),
                //onCancel
                ctx -> ctx.rollback(fileService)
        );
    }


    // ======== HELPERS ======== //
    interface FileOperationContext {
        Mono<Void> cleanup(FileService fileService);
        Mono<Void> rollback(FileService fileService);
    }

    private static class CreateContext implements FileOperationContext{
        String thumbnailPath;
        String videoPath;

        @Override
        public Mono<Void> cleanup(FileService fileService) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> rollback(FileService fileService) {
            return Flux.just(thumbnailPath, videoPath)
                    .flatMap(fileService::deleteFile)
                    .then();
        }
    }

    private static class UpdateContext implements FileOperationContext{
        String oldThumbnailPath;
        String newThumbnailPath;
        VideoEntity existingVideoEntity;

        @Override
        public Mono<Void> cleanup(FileService fileService) {
            if (!oldThumbnailPath.equals(newThumbnailPath))
                return fileService.deleteFile(oldThumbnailPath);
            return Mono.empty();
        }

        @Override
        public Mono<Void> rollback(FileService fileService) {
            return fileService.deleteFile(newThumbnailPath);
        }
    }

    private static class DeleteContext implements FileOperationContext{
        String thumbnailPath;
        String videoPath;

        @Override
        public Mono<Void> cleanup(FileService fileService){
            return Flux.just(thumbnailPath, videoPath)
                    .flatMap(fileService::deleteFile)
                    .then();
        }

        @Override
        public Mono<Void> rollback(FileService fileService) {
            return Mono.empty();
        }
    }


    // ======== ENTITY-DTO ======== //
    private Mono<VideoEntity> createVideoEntity(CreateVideoRequest request, String thumbnailPath, String videoPath){
        VideoEntity videoEntity = new VideoEntity(
                request.name(),
                request.description(),
                thumbnailPath,
                videoPath
        );
        return videoRepository.save(videoEntity);
    }

    private Mono<VideoEntity> updateVideoEntity(VideoEntity existingVideo, UpdateVideoRequest request, String newThumbnailPath) {
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
        updatedVideo.setThumbnailPath(
                newThumbnailPath != null
                        ? newThumbnailPath
                        : existingVideo.getThumbnailPath()
        );
        updatedVideo.setVideoPath(existingVideo.getVideoPath());
        updatedVideo.setCreatedAt(existingVideo.getCreatedAt());
        return videoRepository.save(updatedVideo);
    }

    private VideoResponse toDomainVideo(VideoEntity video){
        return new VideoResponse(
                video.getId(),
                video.getName(),
                video.getDescription(),
                video.getThumbnailPath(),
                video.getVideoPath(),
                video.getCreatedAt()
        );
    }
}
