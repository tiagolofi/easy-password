package com.github.tiagolofi.repository;

import java.time.LocalDateTime;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "logs")
public record Logs(
    LocalDateTime timestamp,
    Metadata metadata
){}
