package com.github.tiagolofi.rest;

import java.util.List;
import java.util.function.Predicate;

import org.jboss.resteasy.reactive.RestQuery;

import com.github.tiagolofi.authentication.jwt.Hashing;
import com.github.tiagolofi.authentication.jwt.PassPhraseCipher;
import com.github.tiagolofi.repository.Item;
import com.github.tiagolofi.repository.ItemRepository;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
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
@Path("/home")
public class Home {

    @Inject
    ItemRepository itemRepository;

    @Inject
    PassPhraseCipher passPhraseCipher;

    @Inject
    Hashing hashing;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance home(List<Item> items);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user"})
    public TemplateInstance homePage() {
        return Templates.home(itemRepository.findAll());
    }

    @PUT
    @RolesAllowed({"admin"})
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editItem(Item item) {
        Predicate<Item> matches = i -> i.service().equals(item.service());
        itemRepository.update(matches, item);
        return Response.ok().build();
    }

    @POST
    @RolesAllowed({"admin"})
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addItem(Item item) {
        itemRepository.persist(item);
        return Response.created(null).build();
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteItem(Item item) {
        itemRepository.delete(item);
        return Response.ok().build();
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/view")
    @Produces(MediaType.TEXT_PLAIN)
    public Response viewPassword(@RestQuery String encryptedPassword, @RestQuery String pin) throws Exception {
        if (hashing.sha256("1111").equals(pin)) {
            return Response.ok(passPhraseCipher.decrypt(encryptedPassword)).build();
        }

        throw new SecurityException("PIN inválido");
    }
}
