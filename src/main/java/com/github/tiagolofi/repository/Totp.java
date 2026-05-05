package com.github.tiagolofi.repository;

public record Totp(
    String value,
    TimedValidation validation
) {}
