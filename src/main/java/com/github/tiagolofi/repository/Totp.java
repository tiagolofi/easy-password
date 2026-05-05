package com.github.tiagolofi.repository;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "totp")
public record Totp(
    String value,
    TimedValidation expiresAt
) {}
