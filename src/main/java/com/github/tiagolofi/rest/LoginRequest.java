package com.github.tiagolofi.rest;

public record LoginRequest(
    String method,
    String username,
    String password,
    String totp
) {}

