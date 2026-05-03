package com.github.tiagolofi.authentication.jwt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenJwt {
    
    public String getToken() { 
        return Jwt
            .issuer("https://github.com.br/tiagolofi")
            .upn("tiagolofi")
            .groups(Set.of("user"))
            .claim("createdAt", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))
            .expiresIn(Duration.ofHours(1))
            .innerSign()
            .encrypt();
    }
}
