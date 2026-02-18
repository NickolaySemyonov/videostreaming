package org.kluthealmighty.videostreaming.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoDTO(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
