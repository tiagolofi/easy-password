package com.github.tiagolofi.rest;

import java.util.List;

import com.github.tiagolofi.authentication.CriptoUtils;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/users")
public class Users {
    
    @Inject
    UserRepository userRepository;

    @Inject
    CriptoUtils criptoUtils;

    @POST
    @Path("/adicionar")
    @RolesAllowed("admin")
    public Response addUser(User user) {
        try {
            User newUser = user
                .withPin(criptoUtils.sha256(user.pin()))
                .withPassword(criptoUtils.encrypt(user.password()))
                .withDefaultRoles();

            userRepository.persist(newUser);

            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/listar")
    @RolesAllowed("admin")
    public List<User> listUsers() {
        return userRepository.listAll();
    }
}