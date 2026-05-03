package com.github.tiagolofi.repository;

import java.util.HashMap;
import java.util.Map;

public class InMemory {
    
    private Map<String, String> data = new HashMap<>();

    public boolean persist(String key, String value) {
        this.data.put(key, value);
        return true;
    }

}
