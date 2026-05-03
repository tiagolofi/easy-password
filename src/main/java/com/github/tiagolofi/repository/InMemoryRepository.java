package com.github.tiagolofi.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class InMemoryRepository<T> {
    
    private Map<UUID, T> data = new HashMap<>();

    public UUID persist(T value) {
        var id = UUID.randomUUID();
        this.data.put(id, value);
        return id;
    }

    public T find(UUID id) {
        return this.data.get(id);
    }

    public Map<UUID, T> findAll() {
        return this.data;
    }

}
