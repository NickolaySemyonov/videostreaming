package org.kluthealmighty.videostreaming.dto;

public record UserResponse(
        Long id,
        String email,
        String passwordHash
) {
}
