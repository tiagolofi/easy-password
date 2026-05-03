package com.github.tiagolofi.repository;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.ConfigProvider;

public class Password {

    public String value;

    private static final String PASS_PHRASE = ConfigProvider.getConfig().getOptionalValue("easy.password.pass.phrase", String.class).orElseThrow();
    
    public Password(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String encrypt() throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(PASS_PHRASE.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(this.value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
