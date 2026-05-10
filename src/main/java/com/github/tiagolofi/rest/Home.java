package com.github.tiagolofi.rest;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.github.tiagolofi.configs.EasyPasswordConfigs;
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

    private static final String ROLE_ADMIN = "admin";

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    JsonWebToken jwt;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance home(boolean isAdmin);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user"})
    public TemplateInstance homePage() {
        boolean isAdmin = jwt.getGroups().contains(ROLE_ADMIN);
        return Templates.home(isAdmin);
    }

}
