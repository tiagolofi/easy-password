package com.github.tiagolofi.rest;

public class LoginRequest {
    public String method;
    public String username;
    public String password;
    public String totp;
    
    public LoginRequest() {
    }
    
    public LoginRequest(String method) {
        this.method = method;
    }
}
