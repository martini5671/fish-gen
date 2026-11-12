package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.HashMap;
import java.util.Map;

public class SettingsControllerTests extends BaseIntegrationTest {

    private static final String SETTINGS_URI = "/api/v1/settings";

    @Test
    void givenNoSavedSettings_whenGetSettings_thenReturnsDefaults() {
        authenticatedRestTestClient().get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.frontBackground").isEqualTo("#dbeafe")
                .jsonPath("$.backBackground").isEqualTo("#2563eb")
                .jsonPath("$.frontText").isEqualTo("dark")
                .jsonPath("$.backText").isEqualTo("light");
    }

    @Test
    void givenSavedSettings_whenGetSettings_thenReturnsThem() {
        RestTestClient client = authenticatedRestTestClient();

        saveSettings(client, settings("#dbeafe", "#2563eb", "dark", "light"))
                .expectStatus()
                .isOk();

        client.get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.frontBackground").isEqualTo("#dbeafe")
                .jsonPath("$.backBackground").isEqualTo("#2563eb")
                .jsonPath("$.frontText").isEqualTo("dark")
                .jsonPath("$.backText").isEqualTo("light");
    }

    @Test
    void givenSavedSettings_whenSaveAgain_thenReplacesThem() {
        RestTestClient client = authenticatedRestTestClient();
        saveSettings(client, settings("#ff0000", "#00ff00", "dark", "light"))
                .expectStatus()
                .isOk();

        // Null backgrounds reset the faces to the default colours
        saveSettings(client, settings(null, null, "light", "dark"))
                .expectStatus()
                .isOk();

        client.get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.frontBackground").isEqualTo("#dbeafe")
                .jsonPath("$.backBackground").isEqualTo("#2563eb")
                .jsonPath("$.frontText").isEqualTo("light")
                .jsonPath("$.backText").isEqualTo("dark");
    }

    @Test
    void givenOtherUserSavedSettings_whenGetSettings_thenDoesNotReturnThem() {
        String otherUserToken = login("other-user", TEST_USER_PASSWORD).accessToken();
        RestTestClient otherUserClient = restTestClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                .build();
        saveSettings(otherUserClient, settings("#ff0000", "#00ff00", "light", "light"))
                .expectStatus()
                .isOk();

        authenticatedRestTestClient().get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.frontBackground").isEqualTo("#dbeafe")
                .jsonPath("$.frontText").isEqualTo("dark");
    }

    @Test
    void givenInvalidBackgroundColour_whenSaveSettings_thenReturns400() {
        saveSettings(authenticatedRestTestClient(), settings("blue", null, "dark", "light"))
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void givenUnknownTextColour_whenSaveSettings_thenReturns400() {
        saveSettings(authenticatedRestTestClient(), settings(null, null, "grey", "light"))
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void givenMissingTextColour_whenSaveSettings_thenReturns400() {
        saveSettings(authenticatedRestTestClient(), settings(null, null, null, "light"))
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void givenInvalidSettings_whenSaveSettings_thenKeepsPreviousSettings() {
        RestTestClient client = authenticatedRestTestClient();
        saveSettings(client, settings("#dbeafe", "#2563eb", "dark", "light"))
                .expectStatus()
                .isOk();

        saveSettings(client, settings("not-a-colour", "#2563eb", "dark", "light"))
                .expectStatus()
                .isBadRequest();

        client.get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.frontBackground").isEqualTo("#dbeafe");
    }

    @Test
    void givenNoToken_whenGetSettings_thenReturns401() {
        restTestClient.get()
                .uri(SETTINGS_URI)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void givenNoToken_whenSaveSettings_thenReturns401() {
        saveSettings(restTestClient, settings("#dbeafe", "#2563eb", "dark", "light"))
                .expectStatus()
                .isUnauthorized();
    }

    private RestTestClient.ResponseSpec saveSettings(RestTestClient client, Map<String, String> body) {
        return client.post()
                .uri(SETTINGS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    // A raw JSON body rather than UserSettingsDto, so invalid values reach the server's validation
    private static Map<String, String> settings(String frontBackground, String backBackground,
                                                String frontText, String backText) {
        Map<String, String> body = new HashMap<>();
        body.put("frontBackground", frontBackground);
        body.put("backBackground", backBackground);
        body.put("frontText", frontText);
        body.put("backText", backText);
        return body;
    }
}
