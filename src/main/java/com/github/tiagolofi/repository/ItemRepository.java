package com.github.tiagolofi.repository;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ItemRepository extends InMemoryRepository<Item> {
    
    @PostConstruct
    public void init() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Gmail", new Password("teste")));
        items.add(new Item("GitHub", new Password("teste")));
        items.add(new Item("Netflix", new Password("teste")));
        persistAll(items);
    }

}
