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
    String pin,
    Set<String> roles
) {
    public User withPin(String pin) {
        return new User(this.id, this.username, this.telegramChatId, this.password, pin, this.roles);
    }

    public User withPassword(Password password) {
        return new User(this.id, this.username, this.telegramChatId, password, this.pin, this.roles);
    }

    public User withDefaultRoles() {
        return new User(this.id, this.username, this.telegramChatId, this.password, this.pin, Set.of("user"));
    }
}
