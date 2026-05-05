package com.github.tiagolofi.repository;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Password {
    public String value;
    public byte[] passPhrase;
    
    public Password(String value) {
        // cria uma passphrase aleatória para cada senha
        this.passPhrase = new byte[16];
        new SecureRandom().nextBytes(this.passPhrase);

        try {
            this.value = encrypt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    public String encrypt(String value) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(this.passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt() throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(this.passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(this.value));
        return new String(decrypted);
    }
}
