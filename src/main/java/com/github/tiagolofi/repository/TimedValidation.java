package com.github.tiagolofi.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimedValidation {
    private final LocalDateTime date;

    public TimedValidation(Long expirationTimeSeconds) {
        this.date = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).plusSeconds(expirationTimeSeconds);
    }

    public LocalDateTime getExpiresAt() {
        return date;
    }

    public boolean isValid() {
        return LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).isBefore(date);
    }
}