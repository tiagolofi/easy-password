package com.github.tiagolofi.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class InMemoryRepository<T> {
    
    private List<T> data = new ArrayList<>();

    public void persist(T value) {
        this.data.add(value);
    }

    public void persistAll(List<T> values) {
        for (T value : values) {
            persist(value);
        }
    }

    public T find(Predicate<T> matches) {
        return data
            .stream()
            .filter(i -> matches.test(i))
            .findFirst()
            .orElse(null);
    }

    public List<T> findAll() {
        return data;
    }

    public void update(Predicate<T> matches, T value) {
        this.data.removeIf(matches);
        this.data.add(value);
    }

    public void delete(T value) {
        this.data.remove(value);
    }

}
