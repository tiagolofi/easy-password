package com.github.tiagolofi.repository;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "service")
public record Service(
    ObjectId id,
    String name, 
    Password password
) {
    public Service withPassword(Password password) {
        return new Service(this.id, this.name, password);
    }
}
