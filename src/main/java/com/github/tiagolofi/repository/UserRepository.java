package com.github.tiagolofi.repository;

import com.github.tiagolofi.models.LoginRequest;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<User> {
    
    public User findByUsername(LoginRequest loginRequest) {
        return findByUsername(loginRequest.username());
    }

    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public boolean exists(String username) {
        return count("username", username) > 0;
    }

}
