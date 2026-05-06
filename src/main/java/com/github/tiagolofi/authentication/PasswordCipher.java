package com.github.tiagolofi.authentication;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.tiagolofi.repository.Password;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordCipher {

    public Password encrypt(Password password) throws Exception {
        byte[] passPhrase = new byte[16];
        new SecureRandom().nextBytes(passPhrase);

        SecretKeySpec secretKey = new SecretKeySpec(passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(password.getValue().getBytes());
        return new Password(Base64.getEncoder().encodeToString(encrypted) + "." + Base64.getEncoder().encodeToString(passPhrase));
    }

    public String decrypt(Password password) throws Exception {
        String[] parts = password.value.split("\\.");
        byte[] passPhrase = Base64.getDecoder().decode(parts[1]);
        SecretKeySpec secretKey = new SecretKeySpec(passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(parts[0]));
        return new String(decrypted);
    }
}
