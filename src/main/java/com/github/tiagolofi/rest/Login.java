package com.github.tiagolofi.rest;

import com.github.tiagolofi.authentication.jwt.TokenJwt;
import com.github.tiagolofi.configs.EasyPasswordConfigs;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/login")
public class Login {

    @Inject
    TokenJwt tokenJwt;

    @Inject
    EasyPasswordConfigs configs;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance login();
    }
    
    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLogin() {
        return Templates.login();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(LoginRequest loginRequest) {
        try {
            AuthenticationMethod authMethod = AuthenticationMethod.fromMethod(loginRequest.method);
            
            // TODO: Implementar autenticação real com JWT
            switch (authMethod) {
                case QRCODE:
                    // TODO: Validar QR Code scaneado
                    return null;
                case TOTP:
                    // TODO: Validar TOTP
                    return null;
                case PASSWORD:
                    // TODO: Validar usuário e senha
                    return loginPassword(loginRequest);
                default:
                    throw new IllegalArgumentException("Método de autenticação inválido");
            }
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Método de autenticação inválido\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Erro ao fazer login\"}")
                .build();
        }
    }

    private Response loginPassword(LoginRequest loginRequest) {
        if (loginRequest.username == null || loginRequest.password == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Usuário ou senha inválidos\"}")
                .build();
        }

        if (configs.admin().equals(loginRequest.username) && configs.adminPassword().equals(loginRequest.password)) {
            return Response.status(Response.Status.OK)
                .entity(tokenJwt.getToken())
                .build();
        }

        return null;
    }
}
