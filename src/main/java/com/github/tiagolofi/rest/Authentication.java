package com.github.tiagolofi.rest;

import java.net.URI;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.github.tiagolofi.authentication.AuthEngine;
import com.github.tiagolofi.authentication.CriptoUtils;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.configs.EasyPasswordConfigs;
import com.github.tiagolofi.models.AuthenticationMethod;
import com.github.tiagolofi.models.LoginRequest;
import com.github.tiagolofi.repository.Otp;
import com.github.tiagolofi.repository.OtpRepository;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.RunOnVirtualThread;
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
    AuthEngine auth;

    @Inject
    EasyPasswordConfigs configs;

    @Inject
    @RestClient
    Telegram telegram;

    @Inject
    OtpRepository otpRepository;
    
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
    @RunOnVirtualThread
    public Response login(LoginRequest loginRequest) {
        try {
            AuthenticationMethod authMethod = AuthenticationMethod.fromMethod(loginRequest.method());
            
            switch (authMethod) {
                case OTP:
                    return loginOtp(loginRequest);
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao fazer login")
                .build();
        }
    }

    @POST
    @PermitAll
    @Path("/otp")
    public Response generateOtp(LoginRequest loginRequest) {
        if (!validarSenha(loginRequest)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Usuário ou senha inválidos")
                .build();
        }

        // Gera o código OTP, salva no banco e envia para o Telegram
        Otp codigo = auth.getOtp(loginRequest.username());
        otpRepository.persist(codigo);

        User user = userRepository.findByUsername(loginRequest.username());

        try {
            telegram.send(configs.telegramBotToken(), user.telegramChatId(), "Seu código de autenticação é: " + codigo.value());
        } catch(Exception e) {
            log.errorf("Código não enviado para: %s", user.telegramChatId());
            otpRepository.removerOtp(codigo);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        
        return Response.status(Response.Status.CREATED).build();
    }

    private Response loginOtp(LoginRequest loginRequest) {
        Otp codigo = otpRepository.findByValue(loginRequest.otp());

        if (codigo == null || !codigo.value().equals(loginRequest.otp())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código OTP inválido")
                .build();
        }

        otpRepository.delete("value", codigo.value());

        if (!codigo.expiresAt().isValid()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Código OTP expirado")
                .build();
        }

        User user = userRepository.findByUsername(codigo.username());

        String token = auth.getToken(user);

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
        if (!validarSenha(loginRequest)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Usuário ou senha inválidos")
                .build();
        }

        User user = userRepository.findByUsername(loginRequest.username());

        String token = auth.getToken(user);
            
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

    private boolean validarSenha(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest);
        if (user == null) {
            throw new IllegalArgumentException("Usuário ou senha inválidos"); 
        }

        String hashedPassword = null;
        try {
            String clearPassword = criptoUtils.decrypt(user.password());
            hashedPassword = criptoUtils.sha256(clearPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return hashedPassword != null && hashedPassword.equals(loginRequest.password());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
                .seeOther(URI.create("/auth"))
                .cookie(expiredAuthCookie)
                .build();
    }
}
