package org.kluthealmighty.videostreaming.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class FileService {

    private final Path uploadDir;
    private final Path backupDir;

    public FileService(
            @Value("${filepath}") Path uploadDir,
            @Value("${tmp_path}") Path tmpDir
    ) {
        this.uploadDir = uploadDir;
        this.backupDir = tmpDir;
        initializeDirectory(uploadDir);
        initializeDirectory(tmpDir);
    }


    /// EXTERNAL
    public Mono<String> saveFile(FilePart filePart) {
        Path targetPath = generateUniquePath(filePart);

        return filePart.transferTo(targetPath)
                .then(Mono.just(targetPath.toString()));
    }



    /**
    * backups, then deletes target file from uploads directory
    * */
    public Mono<String> deleteFile(String filePath) {
        if (isPathEmpty(filePath)) return Mono.empty();

        return Mono.defer(() -> {
            Path originPath = Paths.get(filePath);
            Path backupPath = backupDir.resolve(originPath.getFileName());

            return createBackup(originPath, backupPath)
                    .flatMap(_ -> deleteSourceFile(originPath))
                    .thenReturn(backupPath.toString());
        });
    }


    /// EXTERNAL
    public Mono<String> updateFile(String oldPath, FilePart newFile) {
        if (isPathEmpty(oldPath)) {
            return Mono.error(new IllegalArgumentException("Path cannot be empty"));
        }

        return Mono.defer(() -> {
            Path oldFilePath = Paths.get(oldPath);
            Path backupPath = backupDir.resolve(oldFilePath.getFileName());
            return createBackup(oldFilePath, backupPath)
                    .then(replaceFile(newFile, oldFilePath));
        });
    }


    public Mono<Void> restoreFromBackup(String originalPath) {
        if (isPathEmpty(originalPath)) {
            return Mono.error(new IllegalArgumentException("Path cannot be empty"));
        }

        return Mono.defer(() -> {
            Path original = Paths.get(originalPath);
            Path backup = backupDir.resolve(original.getFileName());

            return Mono.fromCallable(() -> {
                if (!Files.exists(backup)) {
                    throw new RuntimeException("No backup found for: " + originalPath);
                }
                Files.copy(backup, original, StandardCopyOption.REPLACE_EXISTING);
                log.info("Restored {} from backup", originalPath);
                return true;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
        });
    }


    public Mono<Void> clearBackup(String path) {
        if (isPathEmpty(path)) return Mono.empty();

        return Mono.defer(() -> {
            Path origin = Paths.get(path);
            Path backup = backupDir.resolve(origin.getFileName());
            return deleteSourceFile(backup).then();
        });
    }



    //========= helpers ==========
    private void initializeDirectory(Path dir) {
        try {
            if (Files.notExists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize upload directory", e);
        }
    }

    private Path generateUniquePath(FilePart filePart){
        String uniqueFilename = System.currentTimeMillis() + "_" + filePart.filename();
        return uploadDir.resolve(uniqueFilename).normalize();
    }

    private boolean isPathEmpty(String path){
        return path == null || path.trim().isEmpty();
    }




    private Mono<Path> deleteSourceFile(Path sourcePath){
        return Mono.fromCallable(() -> {
            Files.deleteIfExists(sourcePath);
            return sourcePath;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> replaceFile(FilePart filePart, Path oldPath) {
        Path targetPath = generateUniquePath(filePart);

        return filePart.transferTo(targetPath)
                .then(deleteSourceFile(oldPath))
                .thenReturn(targetPath.toString());
    }

    private Mono<Path> createBackup(Path originPath, Path backupPath) {
        // Files.copy() - blocking i/o, thus wrapped with fromCallable() and subscribed on scheduler

        return Mono.fromCallable(() -> {
            if (Files.exists(originPath)) {
                Files.copy(originPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Backed up old file to: {}", backupPath);
            }
            if (!Files.exists(backupPath)) throw new IOException("Error when backup");

            return backupPath;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }




//    public Mono<String> saveFile(FilePart filePart) {
//        String uniqueFilename = System.currentTimeMillis() + "_" + filePart.filename();
//        Path targetPath = uploadDir.resolve(uniqueFilename).normalize();
//
//        return filePart.transferTo(targetPath)
//                .then(Mono.just(targetPath.toString()))
//                .doOnSuccess(path -> log.info("File saved: {}", path))
//                .doOnError(error -> log.error("Failed to save file", error));
//    }
//
//
//    public Mono<Void> deleteFile(String filePath) {
//        return Mono.fromRunnable(() -> {
//            try {
//                Files.deleteIfExists(Paths.get(filePath));
//                log.info("Deleted file: {}", filePath);
//            } catch (IOException e) {
//                log.error("Failed to delete file: {}", filePath, e);
//            }
//        });
//    }
//
//    public Mono<String> updateFile(String oldPath, FilePart newFile) {
//        return deleteFile(oldPath)
//                .then(saveFile(newFile));
//    }


}
