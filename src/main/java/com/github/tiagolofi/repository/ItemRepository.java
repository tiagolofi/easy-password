package com.github.tiagolofi.repository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ItemRepository extends InMemoryRepository<Item> {
    
}
