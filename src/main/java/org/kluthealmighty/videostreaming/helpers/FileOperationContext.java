package org.kluthealmighty.videostreaming.helpers;

import org.kluthealmighty.videostreaming.service.FileService;
import reactor.core.publisher.Mono;

public interface FileOperationContext {
    Mono<Void> cleanup(FileService fileService);
    Mono<Void> rollback(FileService fileService);
}

