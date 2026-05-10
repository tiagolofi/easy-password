package com.github.tiagolofi.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimedValidation {
    private LocalDateTime expirationDate;

    public TimedValidation() {}

    public TimedValidation(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public boolean isValid() {
        return LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).isBefore(this.expirationDate);
    }
}