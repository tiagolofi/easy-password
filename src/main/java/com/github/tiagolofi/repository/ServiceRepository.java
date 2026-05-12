package com.github.tiagolofi.repository;

import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRepository implements PanacheMongoRepository<Service> {

    public Service findByName(String name) {
        return find("name", name).firstResult();
    }

    public List<String> findByOwner(String username) {
        return list("owner", username)
            .stream()
            .map(s -> s.name())
            .sorted()
            .toList();
    }

}
