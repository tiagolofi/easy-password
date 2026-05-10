package com.github.tiagolofi.models;

public record LoginRequest(
    String method,
    String username,
    String password,
    String totp
) {}

