package com.github.tiagolofi.rest;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import com.github.tiagolofi.authentication.AuthenticationMethods;
import com.github.tiagolofi.authentication.Hashing;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Totp;
import com.github.tiagolofi.repository.TotpRepository;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

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
    
    @Inject
    UserRepository userRepository;

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
    @Produces(MediaType.TEXT_PLAIN)
    public Response generateTotp(@RestQuery String username) {
        // Consulta o chatId do usuário
        User user = userRepository.find("username", username).firstResult();
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity("Requisição não permitida.")
                .build();
        }

        // Gera o código TOTP, salva no banco e envia para o Telegram
        Totp codigo = methods.getTotp(username);
        totpRepository.persist(codigo);

        telegram.send(configs.telegramBotToken(), user.telegramChatId(), "Seu código de autenticação é: " + codigo.value());
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(LoginRequest loginRequest) {
        try {
            AuthenticationMethod authMethod = AuthenticationMethod.fromMethod(loginRequest.method());
            
            switch (authMethod) {
                case TOTP:
                    return loginTotp(loginRequest);
                case PASSWORD:
                    return loginPassword(loginRequest);
                default:
                    throw new IllegalArgumentException("Método de autenticação inválido");
            }
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Método de autenticação inválido")
                .build();
        } catch (Exception e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao fazer login")
                .build();
        }
    }

    private Response loginTotp(LoginRequest loginRequest) {
        Totp codigo = totpRepository.find("value", loginRequest.totp()).firstResult();

        if (codigo == null || !codigo.value().equals(loginRequest.totp())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP inválido")
                .build();
        }

        totpRepository.delete("value", codigo.value());

        if (!codigo.expiresAt().isValid()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP expirado")
                .build();
        }

        return Response.status(Response.Status.OK)
            .entity(methods.getToken(String.format("telegramUser%s", codigo.username()), Set.of(configs.adminRoles())))
            .build();
    }

    private Response loginPassword(LoginRequest loginRequest) {
        User user = userRepository.find("username", loginRequest.username()).firstResult();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Usuário ou senha inválidos")
                .build();
        }

        String hashedPassword = null;
        try {
            hashedPassword = hashing.sha256(user.password().decrypt());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (hashedPassword != null && hashedPassword.equals(loginRequest.password())) {
                return Response.status(Response.Status.OK)
                    .entity(methods.getToken(loginRequest.username(), user.roles()))
                    .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Usuário ou senha inválidos")
                .build();
    }
}
