package com.github.tiagolofi.rest;

import java.net.URI;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import com.github.tiagolofi.authentication.AuthenticationMethods;
import com.github.tiagolofi.authentication.CriptoUtils;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.repository.Totp;
import com.github.tiagolofi.repository.TotpRepository;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/auth")
public class Authentication {

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

    @Inject
    CriptoUtils criptoUtils;

    @Inject
    Logger log;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance authentication();
    }
    
    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLogin() {
        return Templates.authentication();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Path("/login")
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

    @POST
    @PermitAll
    @Path("/totp")
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

        try {
            telegram.send(configs.telegramBotToken(), user.telegramChatId(), "Seu código de autenticação é: " + codigo.value());
        } catch(Exception e) {
            log.errorf("Código não enviado para: %s", user.telegramChatId());
            totpRepository.delete("value", codigo.value());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        
        return Response.status(Response.Status.CREATED).build();
    }

    private Response loginTotp(LoginRequest loginRequest) {
        Totp codigo = totpRepository.find("value", loginRequest.totp()).firstResult();

        if (codigo == null || !codigo.value().equals(loginRequest.totp())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP inválido")
                .build();
        }

        totpRepository.delete("value", codigo.value());

        System.out.println(codigo);
        System.out.println(codigo.expiresAt().getExpirationDate());

        if (!codigo.expiresAt().isValid()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código TOTP expirado")
                .build();
        }

        User user = userRepository.find("username", codigo.username()).firstResult();

        String token = methods.getToken(user.username(), user.roles());

        NewCookie cookie = new NewCookie.Builder("Authorization")
            .value(token)
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite(NewCookie.SameSite.STRICT)
            .maxAge(1800)
            .build();

        return Response.seeOther(URI.create("/home")).cookie(cookie).build();
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
            String clearPassword = criptoUtils.decrypt(user.password());
            hashedPassword = criptoUtils.sha256(clearPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (hashedPassword != null && hashedPassword.equals(loginRequest.password())) {
                String token = methods.getToken(user.username(), user.roles());
                NewCookie cookie = new NewCookie.Builder("Authorization")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(NewCookie.SameSite.STRICT)
                    .maxAge(1800)
                    .build();

                return Response.seeOther(URI.create("/home")).cookie(cookie).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Usuário ou senha inválidos")
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Path("/logout")
    public Response logout() {
        NewCookie expiredAuthCookie = new NewCookie.Builder("Authorization")
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .build();

        return Response
                .seeOther(URI.create("/login"))
                .cookie(expiredAuthCookie)
                .build();
    }
}
