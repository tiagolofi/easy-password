package com.github.tiagolofi.rest;

import java.util.List;

import com.github.tiagolofi.authentication.Hashing;
import com.github.tiagolofi.authentication.PassPhraseCipher;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Service;
import com.github.tiagolofi.repository.ServiceRepository;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/home")
public class Home {

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    PassPhraseCipher passPhraseCipher;

    @Inject
    Hashing hashing;

    @Inject
    EasyPasswordConfigs configs;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance home(List<Service> services);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user"})
    public TemplateInstance homePage() {
        return Templates.home(serviceRepository.listAll());
    }

}
