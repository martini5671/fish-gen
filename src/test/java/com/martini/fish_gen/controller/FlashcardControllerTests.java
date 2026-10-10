package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import com.martini.fish_gen.exception.FlashCardGenerationException;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import com.martini.fish_gen.service.FlashcardGeneratorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class FlashcardControllerTests extends BaseIntegrationTest {

    private static final String TEXT_TO_SCAN = "hola soy dora";

    @MockitoBean
    private FlashcardGeneratorService flashcardGeneratorService;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardCollectionRepository flashcardCollectionRepository;

    @Test
    void givenText_whenCreateAiFlashcards_thenOneCollectionIsCreatedAndMultipleFlashcards() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(TEXT_TO_SCAN, TargetLanguage.POLISH))
                .thenReturn(List.of(
                        GeneratedFlashcard.builder().source("hola").target("cześć").build(),
                        GeneratedFlashcard.builder().source("soy").target("jestem").build()));

        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .collectionName("test")
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isCreated();

        List<FlashcardCollection> collections = flashcardCollectionRepository.findAll();
        assertThat(collections).hasSize(1);
        FlashcardCollection collection = collections.getFirst();
        assertThat(collection.getName()).isEqualTo("test");
        assertThat(collection.getText()).isEqualTo(TEXT_TO_SCAN);
        assertThat(collection.getCreatedAt()).isNotNull();
        // Looked up through the owner because the lazy user association can't be read outside a transaction
        assertThat(flashcardCollectionRepository.findByIdIsAndUser_Username(collection.getId(), TEST_USER_USERNAME))
                .as("collection is owned by the logged-in user")
                .isPresent();

        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(collection.getId()))
                .extracting(Flashcard::getSource, Flashcard::getTarget, Flashcard::getTargetLanguage)
                .containsExactly(
                        tuple("hola", "cześć", TargetLanguage.POLISH),
                        tuple("soy", "jestem", TargetLanguage.POLISH));
    }

    @Test
    void givenNoCollectionName_whenCreateAiFlashcards_thenCollectionNameIsGenerated() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(TEXT_TO_SCAN, TargetLanguage.ENGLISH))
                .thenReturn(List.of(GeneratedFlashcard.builder().source("hola").target("hello").build()));

        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .targetLanguage(TargetLanguage.ENGLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isCreated();

        List<FlashcardCollection> collections = flashcardCollectionRepository.findAll();
        assertThat(collections).hasSize(1);
        assertThat(collections.getFirst().getName()).isNotBlank().contains("_");
        assertThat(flashcardRepository.findAll()).hasSize(1);
    }

    @Test
    void givenGeneratorReturnsNoFlashcards_whenCreateAiFlashcards_thenEmptyCollectionIsCreated() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(TEXT_TO_SCAN, TargetLanguage.POLISH))
                .thenReturn(List.of());

        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .collectionName("empty")
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isCreated();

        assertThat(flashcardCollectionRepository.findAll()).hasSize(1);
        assertThat(flashcardRepository.findAll()).isEmpty();
    }

    @Test
    void givenGeneratorFails_whenCreateAiFlashcards_thenReturns500AndNothingIsPersisted() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(TEXT_TO_SCAN, TargetLanguage.POLISH))
                .thenThrow(new FlashCardGenerationException("Ai agent failed to generate flashcards",
                        new RuntimeException("timeout")));

        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .collectionName("test")
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .is5xxServerError();

        assertThat(flashcardCollectionRepository.findAll()).isEmpty();
        assertThat(flashcardRepository.findAll()).isEmpty();
    }

    @Test
    void givenTooShortText_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan("hola")
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {},
                        fields -> assertThat(fields).containsExactly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
        assertThat(flashcardCollectionRepository.findAll()).isEmpty();
    }

    @Test
    void givenTooLongText_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan("a".repeat(3001))
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {},
                        fields -> assertThat(fields).containsExactly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenBlankText_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan("            ")
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {},
                        fields -> assertThat(fields).containsOnly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenNoTargetLanguage_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {},
                        fields -> assertThat(fields).containsExactly("targetLanguage"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenTooLongCollectionName_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .collectionName("a".repeat(256))
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {},
                        fields -> assertThat(fields).containsExactly("collectionName"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenUnknownTargetLanguage_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"textToScan": "hola soy dora", "targetLanguage": "`KLINGON`"}
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Malformed request");

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionWithFlashcards_whenGetByCollectionId_thenReturnsFlashcardsOrderedById() {
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", 1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[*].id").value(new ParameterizedTypeReference<List<Long>>() {},
                        ids -> assertThat(ids).containsExactly(1L, 2L, 3L))
                .jsonPath("$[0].source").isEqualTo("fox")
                .jsonPath("$[0].target").isEqualTo("lis")
                .jsonPath("$[0].targetLanguage").isEqualTo("POLISH");
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionWithoutFlashcards_whenGetByCollectionId_thenReturnsEmptyList() {
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", 11)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionOfOtherUser_whenGetByCollectionId_thenReturns404() {
        // Collection 13 belongs to other-user; 404 rather than 403 so ids of other users' collections can't be probed
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", 13)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void givenNoToken_whenCreateAiFlashcards_thenReturns401() {
        restTestClient.post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateFlashcardsDto.builder()
                        .collectionName("test")
                        .targetLanguage(TargetLanguage.POLISH)
                        .textToScan(TEXT_TO_SCAN)
                        .build())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        Mockito.verifyNoInteractions(flashcardGeneratorService);
        assertThat(flashcardCollectionRepository.findAll()).isEmpty();
    }

    @Test
    void givenNoCollectionExists_whenGetByCollectionId_thenReturns404() {
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", 999)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Collection with id 999 not found");
    }

    @Test
    void givenNonNumericCollectionId_whenGetByCollectionId_thenReturns400() {
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", "abc")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Invalid parameter");
    }
}
