package com.github.tiagolofi.rest;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.github.tiagolofi.authentication.AuthenticationMethods;
import com.github.tiagolofi.authentication.Hashing;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Totp;
import com.github.tiagolofi.repository.TotpRepository;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
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
    Hashing hashing;

    @Inject
    AuthenticationMethods methods;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    @RestClient
    Telegram telegram;

    @Inject
    TotpRepository totpRepository;

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
    @PermitAll
    @Path("/totp")
    public Response generateTotp() {
        Totp codigo = methods.getTotp();
        totpRepository.persist(codigo);
        telegram.send(configs.telegramBotToken(), configs.telegramChatId(), "Seu código de autenticação é: " + codigo.value());
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @PermitAll
    public Response login(LoginRequest loginRequest) {
        try {
            AuthenticationMethod authMethod = AuthenticationMethod.fromMethod(loginRequest.method());
            
            switch (authMethod) {
                case TOTP:
                    return loginTotp(loginRequest, configs.telegramChatId());
                case PASSWORD:
                    // return loginPassword(loginRequest);
                default:
                    throw new IllegalArgumentException("Método de autenticação inválido");
            }
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Método de autenticação inválido")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao fazer login")
                .build();
        }
    }

    private Response loginTotp(LoginRequest loginRequest, Long chatId) {
        Totp codigo = totpRepository.find("value", loginRequest.totp()).firstResult();

        totpRepository.delete("value", codigo.value());

        if (codigo.value() == null || !codigo.value().equals(loginRequest.totp())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP inválido")
                .build();
        }

        if (!codigo.expiresAt().isValid()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP expirado")
                .build();
        }

        return Response.status(Response.Status.OK)
            .entity(methods.getToken(String.format("telegramUser%s", chatId), Set.of(configs.adminRoles())))
            .build();
    }

    // private Response loginPassword(LoginRequest loginRequest) {
    //     String hashedPassword = hashing.sha256(configs.adminPassword());

    //     if (configs.admin().equals(loginRequest.username) && hashedPassword.equals(loginRequest.password)) {
    //         return Response.status(Response.Status.OK)
    //             .entity(tokenJwt.getToken(loginRequest.username, Set.of(configs.adminRoles())))
    //             .build();
    //     }

    //     return Response.status(Response.Status.UNAUTHORIZED)
    //             .entity("{\"error\": \"Usuário ou senha inválidos\"}")
    //             .build();
    // }
}
