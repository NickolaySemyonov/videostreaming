package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.VideoDataRequest;
import org.kluthealmighty.videostreaming.dto.VideoResponse;
import org.kluthealmighty.videostreaming.security.JwtPrincipal;
import org.kluthealmighty.videostreaming.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<VideoResponse>> getVideo(@PathVariable UUID id) {
        return videoService.findVideoById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<VideoResponse>>> getAllVideos() {
        return videoService.findAllVideos()
                .collectList()
                .map(list -> {
                    if (list.isEmpty()) {
                        return ResponseEntity.noContent().build();
                    }
                    return ResponseEntity.ok(Flux.fromIterable(list));
                });
    }

    @PostMapping
    public Mono<ResponseEntity<VideoResponse>> createVideo(
            @RequestPart("videoToCreate") VideoDataRequest request,
            @RequestPart("thumbnail") FilePart thumbnailPart,
            @RequestPart("video") FilePart videoPart,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return videoService.createVideo(request, thumbnailPart, videoPart, principal.userId())
                .map(videoResponse -> ResponseEntity.status(HttpStatus.CREATED).body(videoResponse));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<VideoResponse>> updateVideo(
            @PathVariable UUID id,
            @RequestPart("videoToUpdate") VideoDataRequest request,
            @RequestPart(value = "thumbnail", required = false) FilePart thumbnailPart,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return videoService.isOwner(principal.userId(), id)
                .flatMap(isOwner -> {
                    if (!isOwner) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return videoService.updateVideo(id, request, thumbnailPart)
                            .map(ResponseEntity::ok);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVideo(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return videoService.isOwner(principal.userId(), id)
                .flatMap(isOwner -> {
                    if (!isOwner) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return videoService.deleteVideo(id)
                            .map(_ -> ResponseEntity.noContent().build());
                });
    }
}
