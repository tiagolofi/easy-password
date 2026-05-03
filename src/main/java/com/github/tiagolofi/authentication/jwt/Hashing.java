package com.github.tiagolofi.authentication.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Hashing {

    private static final String SHA_256 = "SHA-256";

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
