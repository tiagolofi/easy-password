package com.github.tiagolofi.repository;

public class Password {
    private String value;
    private String key;
    private String salt;
    
    public Password() {}

    public Password(String value, String key, String salt) {
        this.value = value;
        this.key = key;
        this.salt = salt;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return this.salt;
    }
}
