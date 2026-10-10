package com.martini.fish_gen.configuration;

import com.martini.fish_gen.model.dto.LoginDto;
import com.martini.fish_gen.model.dto.TokenDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@AutoConfigureRestTestClient
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "/sql/users-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTest {

    public static final PostgreSQLContainer POSTGRES;
    public static final String DATABASE_NAME = "fishgen";
    public static final String USERNAME = "test";
    public static final String PASSWORD = "test";
    public static final String DOCKER_IMAGE = "postgres:17";

    // Matches the user inserted by /sql/users-test-data.sql
    public static final String TEST_USER_USERNAME = "test-user";
    public static final String TEST_USER_PASSWORD = "password";

    @Autowired
    protected RestTestClient restTestClient;


    static {
        POSTGRES = new PostgreSQLContainer(DOCKER_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Reference the shared container
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    public RestTestClient getAuthenticatedRestTestClient() {
        TokenDto tokenDto = login(TEST_USER_USERNAME, TEST_USER_PASSWORD);
        return restTestClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.accessToken())
                .build();
    }

    protected TokenDto login(String username, String password) {
        return restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginDto(username, password))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TokenDto.class)
                .returnResult()
                .getResponseBody();
    }


}
