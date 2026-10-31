package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class TokenControllerTests extends BaseIntegrationTest {

    @Test
    void givenLoggedInUser_whenGetTokenUsage_thenReturnsTheirAiCredits() {
        // test-user has 1000 credits in /sql/users-test-data.sql
        authenticatedRestTestClient().get()
                .uri("/api/v1/usage")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.aiCredits").isEqualTo(1000);
    }

    @Test
    void givenOtherUser_whenGetTokenUsage_thenReturnsOnlyTheirOwnCredits() {
        // other-user has no credits, so a value of 0 shows the lookup is scoped to the caller
        String accessToken = login("other-user", TEST_USER_PASSWORD).accessToken();

        restTestClient.get()
                .uri("/api/v1/usage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.aiCredits").isEqualTo(0);
    }

    @Test
    void givenNoToken_whenGetTokenUsage_thenReturns401() {
        restTestClient.get()
                .uri("/api/v1/usage")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
