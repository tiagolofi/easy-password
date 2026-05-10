package com.github.tiagolofi.repository;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "service")
public record Service(
    ObjectId id,
    String name, 
    String owner,
    Password password
) {
    public Service withOwner(String owner) {
        return new Service(this.id, this.name, owner, this.password);
    }

    public Service withPassword(Password password) {
        return new Service(this.id, this.name, this.owner, password);
    }
}
