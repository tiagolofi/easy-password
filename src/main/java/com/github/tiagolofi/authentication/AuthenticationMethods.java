package com.github.tiagolofi.authentication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.Set;

import com.github.tiagolofi.repository.TimedValidation;
import com.github.tiagolofi.repository.Totp;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthenticationMethods {
    
    private static final long EXPIRATION_TIME_SECONDS = 30L; // 30 seconds

    public String getToken(String upn, Set<String> roles) { 
        return Jwt
            .issuer("https://github.com.br/tiagolofi")
            .upn(upn)
            .groups(roles)
            .claim("createdAt", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))
            .expiresIn(Duration.ofHours(1))
            .innerSign()
            .encrypt();
    }

    public Totp getTotp() {
        return new Totp(
            String.valueOf(99999 + new Random().nextInt(1, 900000)), 
            new TimedValidation(EXPIRATION_TIME_SECONDS)
        ); 
    }
}
