package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import com.martini.fish_gen.model.dto.UpdateCollectionNameDto;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class CollectionControllerTests extends BaseIntegrationTest {

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCorrectDateFilter_whenGetCollections_shouldReturnFilteredCollections() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("createdFrom", "2026-09-27")
                        .queryParam("createdTo", "2026-09-30")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactlyInAnyOrder(11L, 12L));
    }


    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenFilterWithName_whenGetCollections_shouldReturnFilteredCollections() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("name", "RED")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactlyInAnyOrder(1L, 2L, 3L));
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenFilterWithNameAndDate_whenGetCollections_shouldReturnFilteredCollections() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("name", "RED")
                        .queryParam("createdFrom", "2026-09-05")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactlyInAnyOrder(3L));
    }

    @Test
    void givenNoCollectionExistsForUpdate_whenUpdateCollectionName_shouldReturn404(){
        var id = 999;
        restTestClient.patch()
                .uri("/api/v1/collections/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(UpdateCollectionNameDto.builder()
                        .collectionName("TEST")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionExistsForUpdate_whenUpdateCollectionName_shouldSucceed(){
        var id = 1;
        restTestClient.patch()
                .uri("/api/v1/collections/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(UpdateCollectionNameDto.builder()
                        .collectionName("TEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("name", "TEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[0].name").isEqualTo("TEST");
    }

}
