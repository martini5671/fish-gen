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
    void givenNoSort_whenGetCollections_shouldReturnNewestFirst() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("size", 5)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactly(12L, 11L, 10L, 9L, 8L));
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenOldestSort_whenGetCollections_shouldReturnOldestFirst() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("size", 5)
                        .queryParam("sort", "OLDEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactly(1L, 2L, 3L, 4L, 5L));
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenNameSort_whenGetCollections_shouldReturnNamesAlphabeticallyIgnoringCase() {
        // Only the first page is asserted: "Red_fox" vs "red panda" depends on the database collation
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("size", 5)
                        .queryParam("sort", "NAME_ASC")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[*].name").value(new ParameterizedTypeReference<List<String>>() {},
                        names -> assertThat(names).containsExactly(
                                "100% useful phrases",
                                "Animals_and_colors",
                                "Business English",
                                "Empty collection",
                                "Green turtle"));
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenLastPage_whenGetCollections_shouldReturnRemainingOwnCollections() {
        // 12 own collections in pages of 5: the third page holds the 2 oldest; other-user's collection never counts
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("page", 2)
                        .queryParam("size", 5)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(12)
                .jsonPath("$.totalPages").isEqualTo(3)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactly(2L, 1L));
    }

    @Test
    void givenUnknownSort_whenGetCollections_shouldReturn400() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("sort", "createdAt")
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void givenTooLargePageSize_whenGetCollections_shouldReturn400() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("size", 101)
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenFilterWithName_whenGetCollections_shouldReturnFilteredCollections() {
        authenticatedRestTestClient().get()
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
                        ids -> assertThat(ids).containsExactlyInAnyOrder(1L, 2L, 3L))
                .jsonPath("$.content[*].targetLanguage").value(new ParameterizedTypeReference<List<String>>() {},
                        languages -> assertThat(languages).containsOnly("POLISH"));
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenFilterWithNameAndSort_whenGetCollections_shouldReturnMatchesInThatOrder() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/collections")
                        .queryParam("name", "RED")
                        .queryParam("sort", "OLDEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactly(1L, 2L, 3L));
    }

    @Test
    void givenNoCollectionExistsForUpdate_whenUpdateCollectionName_shouldReturn404(){
        var id = 999;
        authenticatedRestTestClient().patch()
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
        authenticatedRestTestClient().patch()
                .uri("/api/v1/collections/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(UpdateCollectionNameDto.builder()
                        .collectionName("TEST")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        authenticatedRestTestClient().get()
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
        authenticatedRestTestClient().patch()
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
        authenticatedRestTestClient().delete()
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
        authenticatedRestTestClient().delete()
                .uri("/api/v1/collections/{id}", OTHER_USER_COLLECTION_ID)
                .exchange()
                .expectStatus()
                .isNotFound();

        assertThat(flashcardCollectionRepository.findById(OTHER_USER_COLLECTION_ID)).isPresent();
        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(OTHER_USER_COLLECTION_ID)).hasSize(1);
    }

    @Test
    void givenNoCollectionExists_whenDelete_shouldReturn404() {
        authenticatedRestTestClient().delete()
                .uri("/api/v1/collections/{id}", 999)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

}
