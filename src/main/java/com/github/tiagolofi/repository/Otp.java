package com.github.tiagolofi.repository;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "otp")
public record Otp(
    String value,
    String username,
    TimedValidation expiresAt
) {}
