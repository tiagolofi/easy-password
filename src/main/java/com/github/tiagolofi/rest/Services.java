package com.github.tiagolofi.rest;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;

import com.github.tiagolofi.authentication.CriptoUtils;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Service;
import com.github.tiagolofi.repository.ServiceRepository;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/services")
public class Services {
    
    @Inject
    CriptoUtils criptoUtils;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwtToken;

    @GET
    @RolesAllowed({"user"})
    @Path("listar")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listar() {
        return serviceRepository
            .listAll()
            .stream()
            .map(s -> s.name())
            .toList();
    }

    @POST
    @RolesAllowed({"admin"})
    @Path("/adicionar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addItem(Service service) throws Exception {
        var newService = service.withPassword(criptoUtils.encrypt(service.password()));
        serviceRepository.persist(newService);
        return Response.created(null).build();
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/apagar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteItem(@RestQuery String name) {
        serviceRepository.delete("name", name);
        return Response.ok().build();
    }

    @POST
    @RolesAllowed({"admin"})
    @Path("/mostrar-senha")
    @Produces(MediaType.TEXT_PLAIN)
    public Response viewPassword(@RestQuery String name, @RestHeader("X-PIN-SECURITY") String pin) throws Exception {
        Service service = serviceRepository.find("name", name).firstResult();

        User user = userRepository.findByUsername(jwtToken.getName());

        if (pin.equals(user.pin())) {
            return Response.ok(criptoUtils.decrypt(service.password())).build();
        }

        throw new SecurityException("PIN inválido");
    }
}
