package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.enums.FilePartType;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MediaService {

    private final FileService fileService;

    public MediaService(FileService fileService) {
        this.fileService = fileService;
    }

    public Mono<Resource> getMedia(String filename, FilePartType type) {
        return fileService.getFileAsResource(filename, type);
    }

    // Convenience methods
    public Mono<Resource> getVideo(String filename) {
        return getMedia(filename, FilePartType.VIDEO);
    }

    public Mono<Resource> getThumbnail(String filename) {
        return getMedia(filename, FilePartType.THUMBNAIL);
    }

    public Mono<Resource> getBanner(String filename) {
        return getMedia(filename, FilePartType.BANNER);
    }

    public Mono<Resource> getMiniature(String filename) {
        return getMedia(filename, FilePartType.MINIATURE);
    }
}
