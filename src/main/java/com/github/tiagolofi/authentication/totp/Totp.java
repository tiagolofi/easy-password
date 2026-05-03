package com.github.tiagolofi.authentication.totp;

import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Totp {

    public String getTotp() {
        return String.valueOf(99999 + new Random().nextInt(900000));
    }
}
