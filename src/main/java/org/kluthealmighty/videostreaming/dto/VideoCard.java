package org.kluthealmighty.videostreaming.dto;

import java.util.UUID;

public record VideoCard(
        UUID videoId,
        String videoName,
        String thumbnailUrl,
        String channelTag,
        String channelName,
        String miniatureUrl
) {
}
