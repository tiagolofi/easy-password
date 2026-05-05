package com.github.tiagolofi.authentication;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.tiagolofi.configs.EasyPasswordConfigs;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PassPhraseCipher {

    @Inject
    EasyPasswordConfigs configs;

    public String encrypt(String password) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(configs.passPhrase().getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedPassword) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(configs.passPhrase().getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decrypted);
    }
}
