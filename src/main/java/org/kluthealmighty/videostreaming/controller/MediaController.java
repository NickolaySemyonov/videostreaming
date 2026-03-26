package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @GetMapping("/video/{filename}")
    public Mono<ResponseEntity<Resource>> streamVideo(@PathVariable String filename) {
        return mediaService.getVideo(filename)
                .map(resource -> {
                    String contentType = determineVideoContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + filename + "\"")
                            .body(resource);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/thumbnail/{filename}")
    public Mono<ResponseEntity<Resource>> getThumbnail(@PathVariable String filename) {
        return mediaService.getThumbnail(filename)
                .map(resource -> {
                    String contentType = determineImageContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + filename + "\"")
                            .body(resource);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/banner/{filename}")
    public Mono<ResponseEntity<Resource>> getBanner(@PathVariable String filename) {
        return mediaService.getBanner(filename)
                .map(resource -> {
                    String contentType = determineImageContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/miniature/{filename}")
    public Mono<ResponseEntity<Resource>> getMiniature(@PathVariable String filename) {
        return mediaService.getMiniature(filename)
                .map(resource -> {
                    String contentType = determineImageContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    private String determineVideoContentType(String filename) {
        if (filename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (filename.endsWith(".webm")) {
            return "video/webm";
        } else if (filename.endsWith(".ogg")) {
            return "video/ogg";
        }
        return "video/mp4"; // default
    }

    private String determineImageContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg"; // default
    }

}
