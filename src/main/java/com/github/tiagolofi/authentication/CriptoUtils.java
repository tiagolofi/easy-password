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
        
        String base64Encrypted = Base64.getEncoder().encodeToString(encrypted);
        String base64Passphrase = Base64.getEncoder().encodeToString(passPhrase);
        
        String mixedBases = interleaveBase64(base64Encrypted, base64Passphrase);
        return new Password(mixedBases);
    }

    public String decrypt(Password password) throws Exception {
        String[] parts = deinterleaveBase64(password.value);
        byte[] passPhrase = Base64.getDecoder().decode(parts[1]);
        SecretKeySpec secretKey = new SecretKeySpec(passPhrase, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(parts[0]));
        return new String(decrypted);
    }

    private String interleaveBase64(String base64Encrypted, String base64Passphrase) {
        StringBuilder mixed = new StringBuilder();
        int maxLength = Math.max(base64Encrypted.length(), base64Passphrase.length());
        
        for (int i = 0; i < maxLength; i++) {
            if (i < base64Encrypted.length()) {
                mixed.append(base64Encrypted.charAt(i));
            }
            if (i < base64Passphrase.length()) {
                mixed.append(base64Passphrase.charAt(i));
            }
        }
        
        return mixed.toString();
    }

    private String[] deinterleaveBase64(String mixed) {
        StringBuilder encrypted = new StringBuilder();
        StringBuilder passphrase = new StringBuilder();
        
        for (int i = 0; i < mixed.length(); i++) {
            if (i % 2 == 0) {
                encrypted.append(mixed.charAt(i));
            } else {
                passphrase.append(mixed.charAt(i));
            }
        }
        
        return new String[]{encrypted.toString(), passphrase.toString()};
    }
}
