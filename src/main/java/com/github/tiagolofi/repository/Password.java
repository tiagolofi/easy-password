package com.github.tiagolofi.repository;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Password {
    public String value;
    public byte[] passPhrase;
    
    public Password(String value) {
        this.value = value;
        this.passPhrase = new byte[16];
        new SecureRandom().nextBytes(this.passPhrase);
    }

    public String encrypt() throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(this.passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(this.value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
