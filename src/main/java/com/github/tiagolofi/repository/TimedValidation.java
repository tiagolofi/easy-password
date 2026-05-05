package com.github.tiagolofi.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimedValidation {
    private final LocalDateTime expiresAt;

    public TimedValidation(Long expirationTimeSeconds) {
        this.expiresAt = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).plusSeconds(expirationTimeSeconds);
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isValid() {
        return LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).isBefore(expiresAt);
    }
}