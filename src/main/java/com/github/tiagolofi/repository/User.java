package com.github.tiagolofi.repository;

import java.util.Set;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "user")
public record User(
    ObjectId id,
    String username,
    Long telegramChatId,
    Password password,
    Set<String> roles
) {}
