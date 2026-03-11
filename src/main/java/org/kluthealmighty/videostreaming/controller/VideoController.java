package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.CreateVideoRequest;
import org.kluthealmighty.videostreaming.dto.UpdateVideoRequest;
import org.kluthealmighty.videostreaming.dto.VideoResponse;
import org.kluthealmighty.videostreaming.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
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
    public Mono<ResponseEntity<VideoResponse>> getVideo(@PathVariable UUID id){
        return videoService.findVideoById(id)
                .map(videoResponse -> ResponseEntity.status(HttpStatus.OK).body(videoResponse));
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<VideoResponse>>> getAllVideos(){
        return videoService.findAllVideo()
                .collectList()
                .map(list -> {
                    if (list.isEmpty())
                        return ResponseEntity.noContent().build();
                    else
                        return ResponseEntity.status(HttpStatus.OK).body(Flux.fromIterable(list));
                });
    }

    @PostMapping
    public Mono<ResponseEntity<VideoResponse>> createVideo(
            @RequestPart("file") FilePart filePart,
            @RequestPart("videoToCreate") CreateVideoRequest request
    ) {
        //String filename = filePart.filename();
        //System.out.println("Receiving file: " + filename);
        //System.out.println(request);

        return videoService.createVideo(filePart, request)
                .map(videoResponse -> ResponseEntity.status(HttpStatus.CREATED).body(videoResponse))
                .onErrorResume(e -> {
                    System.err.println("Error: " + e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }


    @PatchMapping("/{id}")
    public Mono<ResponseEntity<VideoResponse>> updateVideoMetadata(
            @PathVariable UUID id,
            @RequestBody UpdateVideoRequest request
    ) {
        return videoService.updateVideoMeta(id, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<VideoResponse>> updateVideo(
            @PathVariable UUID id,
            @RequestPart("file") FilePart filePart,
            @RequestPart("videoToUpdate") UpdateVideoRequest videoToUpdate
    ) {
        return videoService.updateVideo(id, filePart, videoToUpdate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVideo(@PathVariable UUID id){
        return videoService.deleteVideo(id).map(_ -> ResponseEntity.noContent().build());
    }
}
