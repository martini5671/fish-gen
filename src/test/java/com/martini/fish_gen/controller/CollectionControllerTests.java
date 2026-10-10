package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.UpdateCollectionNameDto;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class CollectionControllerTests extends BaseIntegrationTest {

    // Owned by other-user in /sql/collections-test-data.sql, so test-user must not see or change it
    private static final long OTHER_USER_COLLECTION_ID = 13L;

    @Autowired
    private FlashcardCollectionRepository flashcardCollectionRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCorrectDateFilter_whenGetCollections_shouldReturnFilteredCollections() {
        getAuthenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("createdFrom", "2026-09-27T00:00:00")
                        .queryParam("createdTo", "2026-09-30T23:59:59")
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
        getAuthenticatedRestTestClient().get()
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
        getAuthenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("name", "RED")
                        .queryParam("createdFrom", "2026-09-05T00:00:00")
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
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenDateTimeFilter_whenGetCollections_shouldApplyTimeOfDayWithInclusiveBounds() {
        // Bounds sit exactly on collections 5 (09-10 23:59:59) and 6 (09-11 00:00:00); 4 from the same day as 5 is excluded by its time
        getAuthenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .queryParam("createdFrom", "2026-09-10T23:59:59")
                        .queryParam("createdTo", "2026-09-11T00:00:00")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactlyInAnyOrder(5L, 6L));
    }

    @Test
    void givenNoCollectionExistsForUpdate_whenUpdateCollectionName_shouldReturn404(){
        var id = 999;
        getAuthenticatedRestTestClient().patch()
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
        getAuthenticatedRestTestClient().patch()
                .uri("/api/v1/collections/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(UpdateCollectionNameDto.builder()
                        .collectionName("TEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        getAuthenticatedRestTestClient().get()
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

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionOfOtherUser_whenUpdateCollectionName_shouldReturn404AndKeepName() {
        getAuthenticatedRestTestClient().patch()
                .uri("/api/v1/collections/{id}", OTHER_USER_COLLECTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(UpdateCollectionNameDto.builder()
                        .collectionName("HACKED")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound();

        assertThat(flashcardCollectionRepository.findById(OTHER_USER_COLLECTION_ID))
                .get()
                .extracting(FlashcardCollection::getName)
                .isEqualTo("Red other-user");
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenOwnCollection_whenDelete_shouldRemoveCollectionWithFlashcards() {
        var id = 1L;
        getAuthenticatedRestTestClient().delete()
                .uri("/api/v1/collections/{id}", id)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(flashcardCollectionRepository.findById(id)).isEmpty();
        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(id)).isEmpty();
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionOfOtherUser_whenDelete_shouldReturn404AndKeepCollection() {
        getAuthenticatedRestTestClient().delete()
                .uri("/api/v1/collections/{id}", OTHER_USER_COLLECTION_ID)
                .exchange()
                .expectStatus()
                .isNotFound();

        assertThat(flashcardCollectionRepository.findById(OTHER_USER_COLLECTION_ID)).isPresent();
        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(OTHER_USER_COLLECTION_ID)).hasSize(1);
    }

    @Test
    void givenNoCollectionExists_whenDelete_shouldReturn404() {
        getAuthenticatedRestTestClient().delete()
                .uri("/api/v1/collections/{id}", 999)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

}
