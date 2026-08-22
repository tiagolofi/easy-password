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

    @Inject
    GpgService gpgService;

    @GET
    @RolesAllowed({"user"})
    @Path("listar")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listar() {
        return serviceRepository.findByOwner(jwtToken.getName());
    }

    @POST
    @RolesAllowed({"user"})
    @Path("/adicionar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addItem(Service service) throws Exception {
        var newService = service
            .withOwner(jwtToken.getName())
            .withPassword(criptoUtils.encrypt(service.password()));
        try {
            serviceRepository.persist(newService);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao adicionar serviço").build();
        }
    }

    @DELETE
    @RolesAllowed({"user"})
    @Path("/apagar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteItem(@RestQuery String name) {
        serviceRepository.delete("name", name);
        return Response.ok().build();
    }

    @POST
    @RolesAllowed({"user"})
    @Path("/mostrar-senha")
    @Produces("application/octet-stream")
    public Response viewPassword(@RestQuery String name, @RestHeader("X-PIN-SECURITY") String pin) throws Exception {
        User user = userRepository.findByUsername(jwtToken.getName());

        if (pin.equals(user.pin())) {
            Service service = serviceRepository.findByName(name);

            String decryptedPassword = criptoUtils.decrypt(service.password());

            byte[] encryptedPassword = gpgService.encrypt(
                decryptedPassword.getBytes(),
                name + ".pwd",
                configs.passphrase()
            );

            return Response.ok(encryptedPassword)
                .type("application/pgp-encrypted")
                .header("Content-Disposition", "attachment; filename=\"" + name + ".pwd.gpg\"")
                .build();
        }

        throw new SecurityException("PIN inválido");
    }
}
