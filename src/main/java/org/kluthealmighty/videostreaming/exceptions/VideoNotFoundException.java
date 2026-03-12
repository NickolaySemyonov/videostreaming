package org.kluthealmighty.videostreaming.exceptions;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(UUID id) {
        super("Video not found with id: " + id);
    }
}
