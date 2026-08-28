package com.github.tiagolofi.authentication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import com.github.tiagolofi.repository.TimedValidation;
import com.github.tiagolofi.repository.Otp;
import com.github.tiagolofi.repository.User;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthEngine {
    
    private static final long EXPIRATION_TIME_SECONDS = 60L; // 60 seconds

    public String getToken(User user) { 
        return Jwt
            .issuer("https://github.com.br/tiagolofi")
            .upn(user.username())
            .groups(user.roles())
            .claim("createdAt", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))
            .expiresIn(Duration.ofMinutes(30))
            .innerSign()
            .encrypt();
    }

    public Otp getOtp(String username) {
        LocalDateTime expirationDate = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).plusSeconds(EXPIRATION_TIME_SECONDS);

        return new Otp(
            String.valueOf(99999 + new Random().nextInt(1, 900000)), 
            username,
            new TimedValidation(expirationDate)
        ); 
    }
}
