package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.enums.FilePartType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class FileService {

    private final Map<FilePartType, Path> typeToDirMap;

    public FileService(
            @Value("${thumbnail_dir}") Path thumbnailDir,
            @Value("${video_dir}") Path videoDir,
            @Value("${banner_dir}") Path bannerDir,
            @Value("${miniature_dir}") Path miniatureDir

    ) {
        this.typeToDirMap = Map.of(
                FilePartType.THUMBNAIL, thumbnailDir,
                FilePartType.VIDEO, videoDir,
                FilePartType.BANNER, bannerDir,
                FilePartType.MINIATURE, miniatureDir
        );

        initializeDirectory(thumbnailDir);
        initializeDirectory(videoDir);
        initializeDirectory(bannerDir);
        initializeDirectory(miniatureDir);
    }

    // region API

    public Mono<Resource> getFileAsResource(String filename, FilePartType type) {
        return getFullPath(filename, type)
                .flatMap(path -> Mono.fromCallable(() -> {
                    Resource resource = new UrlResource(path.toUri());
                    if (resource.exists() && resource.isReadable()) {
                        return resource;
                    }
                    throw new RuntimeException("File not readable: " + filename);
                }).subscribeOn(Schedulers.boundedElastic()));
    }


    public Mono<String> saveFile(FilePart filePart, FilePartType type) {
        Path uploadDir = typeToDirMap.get(type);
        String uniqueFilename = generateUniqueFilename(filePart);
        Path targetPath = uploadDir.resolve(uniqueFilename).normalize();
        return filePart.transferTo(targetPath)
                .then(Mono.just(uniqueFilename)); // Return only filename, not full path


    }

    public Mono<Void> deleteFile(String filename, FilePartType type) {
        if (isPathEmpty(filename)) return Mono.empty();

        return getFullPath(filename, type)
                .flatMap(path -> Mono.fromRunnable(() -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + filename, e);
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
    // endregion

    // region HELPERS
    private void initializeDirectory(Path dir) {
        try {
            if (Files.notExists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize upload directory", e);
        }
    }


    private String generateUniqueFilename(FilePart filePart) {
        // Use timestamp + original filename to ensure uniqueness
        return System.currentTimeMillis() + "_" + filePart.filename();
    }

    private Mono<Path> getFullPath(String filename, FilePartType type) {
        return Mono.fromCallable(() -> {
            Path baseDir = typeToDirMap.get(type);
            if (baseDir == null) {
                throw new IllegalArgumentException("Invalid file type: " + type);
            }

            Path fullPath = baseDir.resolve(filename).normalize();

            // Security: ensure the resolved path is still within the base directory
            if (!fullPath.startsWith(baseDir)) {
                throw new SecurityException("Access denied: path traversal detected");
            }

            if (Files.exists(fullPath)) {
                return fullPath;
            }
            throw new RuntimeException("File not found: " + filename);
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private boolean isPathEmpty(String path){
        return path == null || path.trim().isEmpty();
    }
    // endregion
}
