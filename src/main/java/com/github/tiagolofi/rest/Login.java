package com.github.tiagolofi.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
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

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance login();
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @PermitAll
    public String loginPage() {
        return Templates.login().render();
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
                    break;
                case TOTP:
                    // TODO: Validar TOTP
                    if (loginRequest.totp == null || loginRequest.totp.isEmpty()) {
                        return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"TOTP inválido\"}")
                            .build();
                    }
                    break;
                case PASSWORD:
                    // TODO: Validar usuário e senha
                    if (loginRequest.username == null || loginRequest.password == null) {
                        return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"Usuário ou senha inválidos\"}")
                            .build();
                    }
                    break;
            }
            
            // Retornar sucesso
            return Response.ok("{\"success\": true, \"message\": \"Login bem-sucedido com \" + authMethod.name()}")
                .build();
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
}
