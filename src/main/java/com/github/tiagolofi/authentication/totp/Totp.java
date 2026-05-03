package com.github.tiagolofi.authentication.totp;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Totp {

    public String getTotp() {
        return "123456";
    }
}
