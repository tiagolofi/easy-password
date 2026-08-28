package com.github.tiagolofi.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OtpRepository implements PanacheMongoRepository<Otp> {
    
    public Otp findByTotp(Otp otp) {
        return findByValue(otp.value());
    }

    public Otp findByValue(String value) {
        return find("value", value).firstResult();
    }

    public void removerOtp(Otp otp) {
        delete("value", otp.value());
    }

}
