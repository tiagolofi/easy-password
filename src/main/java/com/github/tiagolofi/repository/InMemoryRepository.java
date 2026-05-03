package com.github.tiagolofi.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class InMemoryRepository<T> {
    
    private Map<UUID, T> data = new HashMap<>();

    public UUID persist(T value) {
        var id = UUID.randomUUID();
        this.data.put(id, value);
        return id;
    }

    public List<UUID> persistAll(List<T> values) {
        List<UUID> ids = new ArrayList<>();
        for (T value : values) {
            ids.add(persist(value));
        }
        return ids;
    }

    public T find(UUID id) {
        return this.data.get(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(this.data.values());
    }

}
