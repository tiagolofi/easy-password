package com.github.tiagolofi.repository;

public class Password {
    public String value;
    
    public Password() {}

    public Password(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
