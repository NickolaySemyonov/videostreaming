package org.kluthealmighty.videostreaming.dto;

public record AuthRequest(
        String email,
        String password
) {
}
