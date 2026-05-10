package com.github.tiagolofi.repository;

import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRepository implements PanacheMongoRepository<Service> {

    public List<Service> findByOwner(String username) {
        return list("owner", username);
    }

}
