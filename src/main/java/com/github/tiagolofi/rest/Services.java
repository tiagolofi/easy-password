package com.github.tiagolofi.rest;

import org.jboss.resteasy.reactive.RestQuery;

import com.github.tiagolofi.authentication.Hashing;
import com.github.tiagolofi.authentication.PasswordCipher;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Service;
import com.github.tiagolofi.repository.ServiceRepository;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/services")
public class Services {
    
    @Inject
    Hashing hashing;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    PasswordCipher passwordCipher;

    @PUT
    @RolesAllowed({"admin"})
    @Path("/alterar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editItem(Service service){ 
        serviceRepository.update(service);
        return Response.ok().build();
    }

    @POST
    @RolesAllowed({"admin"})
    @Path("/adicionar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addItem(Service service) {
        serviceRepository.persist(service);
        return Response.created(null).build();
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/apagar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteItem(Service service) {
        serviceRepository.delete(service);
        return Response.ok().build();
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/mostrar-senha")
    @Produces(MediaType.TEXT_PLAIN)
    public Response viewPassword(Service service, @RestQuery String pin) throws Exception {
        if (hashing.sha256(configs.pin()).equals(pin)) {
            return Response.ok(passwordCipher.decrypt(service.password())).build();
        }

        throw new SecurityException("PIN inválido");
    }
}
