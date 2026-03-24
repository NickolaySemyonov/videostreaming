package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.enums.FilePartType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final Path thumbnailDir;
    private final Path videoDir;
    private final Path bannerDir;
    private final Path miniatureDir;

    public FileService(
            @Value("${thumbnail_dir}") Path thumbnailDir,
            @Value("${video_dir}") Path videoDir,
            @Value("${banner_dir}") Path bannerDir,
            @Value("${miniature_dir}") Path miniatureDir

    ) {
        this.thumbnailDir = thumbnailDir;
        this.videoDir = videoDir;
        this.bannerDir = bannerDir;
        this.miniatureDir = miniatureDir;

        initializeDirectory(thumbnailDir);
        initializeDirectory(videoDir);
        initializeDirectory(bannerDir);
        initializeDirectory(miniatureDir);
    }

    // region API
    public Mono<String> saveFile(FilePart filePart, FilePartType type) {
        Path uploadDir = switch (type){
            case THUMBNAIL -> thumbnailDir;
            case VIDEO -> videoDir;
            case BANNER -> bannerDir;
            case MINIATURE -> miniatureDir;
        };

        Path targetPath = generateUniquePath(filePart, uploadDir);

        return filePart.transferTo(targetPath)
                .then(Mono.just(targetPath.toString()));
    }

    public Mono<Void> deleteFile(String filePath) {
        if (isPathEmpty(filePath)) return Mono.empty();

        Path path = Paths.get(filePath);
        return Mono.fromCallable(() -> {
            Files.deleteIfExists(path);
            return path;
        }).then().subscribeOn(Schedulers.boundedElastic());
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

    private Path generateUniquePath(FilePart filePart, Path uploadDir){
        String uniqueFilename = System.currentTimeMillis() + "_" + filePart.filename();
        return uploadDir.resolve(uniqueFilename).normalize();
    }

    private boolean isPathEmpty(String path){
        return path == null || path.trim().isEmpty();
    }
    // endregion
}
