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

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public Password encrypt(Password password) throws Exception {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        byte[] salt = generateSalt();
        
        String base64Salt = Base64.getEncoder().encodeToString(salt);

        String combined = password.getValue() + base64Salt;

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(combined.getBytes(StandardCharsets.UTF_8));

        String base64Encrypted = Base64.getEncoder().encodeToString(encrypted);

        // Cometendo o crime de armazenar a chave de criptografia junto com a senha criptografada, mas pelo menos é uma chave aleatória e não fixa
        String base64Key = Base64.getEncoder().encodeToString(key);

        return new Password(base64Encrypted, base64Key, base64Salt);
    }

    public String decrypt(Password password) throws Exception {
        byte[] key = Base64.getDecoder().decode(password.getKey());
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(password.getValue()));

        String decryptedString = new String(decrypted, StandardCharsets.UTF_8);
        
        return decryptedString.substring(0, decryptedString.indexOf(password.getSalt()));
    }
}
