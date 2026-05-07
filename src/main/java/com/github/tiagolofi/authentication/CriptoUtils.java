package com.github.tiagolofi.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.tiagolofi.repository.Password;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CriptoUtils {

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
