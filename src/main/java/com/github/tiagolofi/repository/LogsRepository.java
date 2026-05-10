package com.github.tiagolofi.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class LogsRepository implements PanacheMongoRepository<Logs> {
    
}
