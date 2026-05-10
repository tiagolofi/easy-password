package com.github.tiagolofi.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tiagolofi.authentication.AuthenticationMethods;
import com.github.tiagolofi.clients.Telegram;
import com.github.tiagolofi.repository.Totp;
import com.github.tiagolofi.repository.TotpRepository;
import com.github.tiagolofi.repository.User;
import com.github.tiagolofi.repository.UserRepository;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

@QuarkusTest
public class AuthenticationTest {
    
    @InjectMock
    UserRepository userRepository;

    @InjectMock
    TotpRepository totpRepository;

    @InjectMock
    @RestClient
    Telegram telegram;

    @InjectMock
    AuthenticationMethods methods;

    @ParameterizedTest
    @MethodSource("cenarios")
    public void testLoginTotp(
        String username, User user, 
        int expectedStatus, int timesFindByUsername, int timesGetTotp, 
        int timesPersist, int timesSend, int timesRemoverTotp, 
        Throwable telegramException) {

        when(userRepository.findByUsername(username)).thenReturn(user);
        Totp totp = mock(Totp.class);
        when(methods.getTotp(username)).thenReturn(totp);
        doNothing().when(totpRepository).persist(totp);
        if (telegramException == null) {
            when(telegram.send(anyString(), anyLong(), anyString())).thenReturn(null);
        } else { 
            when(telegram.send(anyString(), anyLong(), anyString())).thenThrow(telegramException);
        }

        given()
            .when()
            .post("/auth/totp?username=" + username)
            .then()
            .statusCode(expectedStatus);

        verify(userRepository, times(timesFindByUsername)).findByUsername(username);
        verify(methods, times(timesGetTotp)).getTotp(username);
        verify(totpRepository, times(timesPersist)).persist(totp);
        verify(telegram, times(timesSend)).send(anyString(), anyLong(), anyString());
        verify(totpRepository, times(timesRemoverTotp)).removerTotp(totp);
    }

    private static Stream<Arguments> cenarios() {
        return Stream.of(
            Arguments.of("userValid", mock(User.class), 201, 1, 1, 1, 1, 0, null),
            Arguments.of("userNotFound", null, 403, 1, 0, 0, 0, 0, null),
            Arguments.of("userTelegramError", mock(User.class), 406, 1, 1, 1, 1, 1, new RuntimeException())
        );
    }

}
