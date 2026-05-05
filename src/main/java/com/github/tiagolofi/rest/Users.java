package com.github.tiagolofi.rest;

import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/users")
public class Users {
    
    @Inject
    UserRepository userRepository;

    @POST
    @Path("/adicionar")
    @RolesAllowed("admin")
    public Response addUser(User user) {
        try {
            userRepository.persist(user);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
