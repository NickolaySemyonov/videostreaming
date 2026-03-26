package org.kluthealmighty.videostreaming.dto;

public record ChannelDataRequest(
        String channelTag,
        String channelName,
        String channelDescription
) {
}
