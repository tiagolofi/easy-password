package com.github.tiagolofi.authentication.jwt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenJwt {
    
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
}
