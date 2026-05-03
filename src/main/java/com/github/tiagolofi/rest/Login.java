package com.github.tiagolofi.rest;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.github.tiagolofi.authentication.jwt.Hashing;
import com.github.tiagolofi.authentication.jwt.TokenJwt;
import com.github.tiagolofi.authentication.totp.Totp;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Codes;
import com.github.tiagolofi.repository.CodesRepository;

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
    TokenJwt tokenJwt;

    @Inject
    Hashing hashing;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    @RestClient
    Telegram telegram;

    @Inject
    Totp totp;

    @Inject
    CodesRepository codesRepository;

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
        String codigo = totp.getTotp();
        codesRepository.persist(new Codes(codigo));
        telegram.send(configs.telegramBotToken(), configs.telegramChatId(), "Seu código de autenticação é: " + codigo);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(LoginRequest loginRequest) {
        try {
            AuthenticationMethod authMethod = AuthenticationMethod.fromMethod(loginRequest.method);
            
            switch (authMethod) {
                case TOTP:
                    return loginTotp(loginRequest, configs.telegramChatId());
                case PASSWORD:
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

    private Response loginTotp(LoginRequest loginRequest, Long chatId) {
        Codes codeRecord = codesRepository.findAll().stream()
            .filter(c -> c.code().equals(loginRequest.totp))
            .findFirst()
            .orElse(null);

        if (codeRecord != null) {
            codesRepository.delete(codeRecord);
            return Response.status(Response.Status.OK)
                .entity(tokenJwt.getToken(String.format("telegramUser%s", chatId), Set.of(configs.adminRoles())))
                .build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Código TOTP inválido\"}")
                .build();
    }

    private Response loginPassword(LoginRequest loginRequest) {
        String hashedPassword = hashing.sha256(configs.adminPassword());

        if (configs.admin().equals(loginRequest.username) && hashedPassword.equals(loginRequest.password)) {
            return Response.status(Response.Status.OK)
                .entity(tokenJwt.getToken(loginRequest.username, Set.of(configs.adminRoles())))
                .build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Usuário ou senha inválidos\"}")
                .build();
    }
}
