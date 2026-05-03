package com.github.tiagolofi.rest;

import java.util.ArrayList;
import java.util.List;

import com.github.tiagolofi.repository.Item;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/home")
public class Home {

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance home(List<Item> items);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @PermitAll()
    public String homePage() {
        // TODO: Buscar items do banco de dados/repositório do usuário autenticado
        List<Item> items = new ArrayList<>();
        items.add(new Item("Gmail", "teste"));
        items.add(new Item("GitHub", "teste"));
        items.add(new Item("Netflix", "teste"));
        
        return Templates.home(items).render();
    }
}
