package org.kluthealmighty.videostreaming.dto;

import java.util.UUID;

public record ExtendedVideoCard(
        UUID videoId,
        String videoName,
        String videoDescription,
        String thumbnailUrl,
        String videoUrl,
        String channelTag,
        String channelName,
        String miniatureUrl,
        boolean editable
) {
}
