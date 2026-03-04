package org.kluthealmighty.videostreaming.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class FileService {
    @Value("${filepath}")
    private String uploadDir;

    public Mono<String> saveFile(FilePart filePart) {
        String filename = filePart.filename();
        String uniqueFilename = System.currentTimeMillis() + "_" + filename;
        String filePath = uploadDir + uniqueFilename;

        createUploadDirectoryIfNeeded();

        return filePart.transferTo(Paths.get(filePath))
                .then(Mono.just(filePath))
                .doOnSuccess(path -> log.info("File saved: {}", path))
                .doOnError(error -> log.error("Failed to save file", error));
    }

    private void createUploadDirectoryIfNeeded() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public Mono<Void> deleteFile(String filePath) {
        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(filePath));
                log.info("Deleted file: {}", filePath);
            } catch (IOException e) {
                log.error("Failed to delete file: {}", filePath, e);
            }
        });
    }

    public Mono<String> updateFile(String oldPath, FilePart newFile) {
        return deleteFile(oldPath)
                .then(saveFile(newFile));
    }


}
