package org.kluthealmighty.videostreaming.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String channelTag,
        String channelName,
        String channelDescription,
        String bannerPath,
        String miniaturePath,
        LocalDateTime createdAt
) {
}
