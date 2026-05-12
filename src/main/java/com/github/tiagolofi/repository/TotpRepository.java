package com.github.tiagolofi.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TotpRepository implements PanacheMongoRepository<Totp> {
    
    public Totp findByTotp(Totp totp) {
        return findByValue(totp.value());
    }

    public Totp findByValue(String value) {
        return find("value", value).firstResult();
    }

    public void removerTotp(Totp totp) {
        delete("value", totp.value());
    }

}
