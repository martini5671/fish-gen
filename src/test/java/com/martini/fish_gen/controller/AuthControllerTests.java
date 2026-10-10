package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import com.martini.fish_gen.model.dto.LoginDto;
import com.martini.fish_gen.model.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTests extends BaseIntegrationTest {

    @Test
    void givenCorrectCredentials_whenLogin_thenReturnsBearerToken() {
        TokenDto token = login(TEST_USER_USERNAME, TEST_USER_PASSWORD);
        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(3600);
    }

    @Test
    void givenTokenFromLogin_whenCallProtectedEndpoint_thenReturns200() {
        TokenDto token = login(TEST_USER_USERNAME, TEST_USER_PASSWORD);

        restTestClient.get()
                .uri("/api/v1/collections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void givenWrongPassword_whenLogin_thenReturns401() {
        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(LoginDto.builder().username(TEST_USER_USERNAME).password("wrong-password").build())
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Authentication failed")
                .jsonPath("$.detail").isEqualTo("Invalid username or password");
    }

    @Test
    void givenUnknownUsername_whenLogin_thenReturns401() {
        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(LoginDto.builder().username("nobody").password(TEST_USER_PASSWORD).build())
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Invalid username or password");
    }

    @Test
    void givenBlankCredentials_whenLogin_thenReturns400() {
        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(LoginDto.builder().username(" ").password("").build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors.length()").isEqualTo(2);
    }

    @Test
    void givenNoToken_whenCallProtectedEndpoint_thenReturns401() {
        restTestClient.get()
                .uri("/api/v1/collections")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void givenInvalidToken_whenCallProtectedEndpoint_thenReturns401() {
        restTestClient.get()
                .uri("/api/v1/collections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
