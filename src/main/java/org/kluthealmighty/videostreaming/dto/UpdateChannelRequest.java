package org.kluthealmighty.videostreaming.dto;

public record UpdateChannelRequest(
        String channelTag,
        String channelName,
        String channelDescription
) {
}
