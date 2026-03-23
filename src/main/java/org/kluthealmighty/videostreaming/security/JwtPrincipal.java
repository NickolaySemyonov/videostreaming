package org.kluthealmighty.videostreaming.security;

public record JwtPrincipal(Long userId, String email) { }