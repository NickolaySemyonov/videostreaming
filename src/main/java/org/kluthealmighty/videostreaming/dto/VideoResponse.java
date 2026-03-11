package org.kluthealmighty.videostreaming.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoResponse(
        UUID id,
        String name,
        String description,
        String path,
        LocalDateTime createdAt
) {
}
