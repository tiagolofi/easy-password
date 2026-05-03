package com.github.tiagolofi.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/")
public class RootResource {
    
    @GET
    @PermitAll
    public Response root() {
        // Redirecionar para login
        return Response.seeOther(java.net.URI.create("/login")).build();
    }
}
