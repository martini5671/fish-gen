package com.martini.fish_gen.controller;

import com.martini.fish_gen.configuration.BaseIntegrationTest;
import com.martini.fish_gen.exception.FlashCardGenerationException;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateCollectionDto;
import com.martini.fish_gen.model.dto.CreateFlashcardsWithAIDto;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.dto.TokenDto;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import com.martini.fish_gen.service.FlashcardGeneratorService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class FlashcardControllerTests extends BaseIntegrationTest {

    private static final String TEXT_TO_SCAN = "hola soy dora";
    private static final Duration GENERATION_TIMEOUT = Duration.ofSeconds(5);

    // Owned by other-user in /sql/collections-test-data.sql
    private static final long OTHER_USER_COLLECTION_ID = 13L;

    @MockitoBean
    private FlashcardGeneratorService flashcardGeneratorService;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardCollectionRepository flashcardCollectionRepository;

    @Test
    void givenOwnCollection_whenCreateAiFlashcards_thenCollectionIsFilledWithFlashcards() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(eq(TEXT_TO_SCAN),
                        eq(TargetLanguage.POLISH),
                        anyString()))
                .thenReturn(List.of(
                        GeneratedFlashcard.builder().source("hola").target("cześć").build(),
                        GeneratedFlashcard.builder().source("soy").target("jestem").build()));
        Long collectionId = createCollection("test");

        requestAiFlashcards(aiRequest(collectionId).build())
                .expectStatus()
                .isAccepted();

        Awaitility.await()
                .atMost(GENERATION_TIMEOUT)
                .untilAsserted(() -> assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(collectionId))
                        .extracting(Flashcard::getSource, Flashcard::getTarget)
                        .containsExactly(
                                tuple("hola", "cześć"),
                                tuple("soy", "jestem")));
        assertThat(flashcardCollectionRepository.findAll())
                .as("generation fills the existing collection instead of creating another one")
                .singleElement()
                .extracting(FlashcardCollection::getName)
                .isEqualTo("test");
    }

    @Test
    void givenText_whenCreateAiFlashcards_thenGeneratorIsCalledWithRequestingUsername() {
        // Generation runs on another thread without the security context, so the username must travel in the event
        Mockito.when(flashcardGeneratorService.generateFlashcards(eq(TEXT_TO_SCAN),
                        eq(TargetLanguage.POLISH),
                        anyString()))
                .thenReturn(List.of(GeneratedFlashcard.builder().source("hola").target("cześć").build()));
        Long collectionId = createCollection("test");

        requestAiFlashcards(aiRequest(collectionId).build())
                .expectStatus()
                .isAccepted();

        awaitGeneratorCalled();
        Mockito.verify(flashcardGeneratorService)
                .generateFlashcards(TEXT_TO_SCAN, TargetLanguage.POLISH, TEST_USER_USERNAME);
    }

    @Test
    void givenGeneratorReturnsNoFlashcards_whenCreateAiFlashcards_thenCollectionStaysEmpty() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(eq(TEXT_TO_SCAN),
                        eq(TargetLanguage.POLISH), anyString()))
                .thenReturn(List.of());
        Long collectionId = createCollection("empty");

        requestAiFlashcards(aiRequest(collectionId).build())
                .expectStatus()
                .isAccepted();

        awaitGeneratorCalled();
        assertThat(flashcardRepository.findAll()).isEmpty();
    }

    @Test
    void givenGeneratorFails_whenCreateAiFlashcards_thenNoFlashcardsAreAdded() {
        Mockito.when(flashcardGeneratorService.generateFlashcards(eq(TEXT_TO_SCAN), eq(TargetLanguage.POLISH),
                        anyString()))
                .thenThrow(new FlashCardGenerationException("Ai agent failed to generate flashcards",
                        new RuntimeException("timeout")));
        Long collectionId = createCollection("test");

        requestAiFlashcards(aiRequest(collectionId).build())
                .expectStatus()
                .isAccepted();

        // The request already succeeded, so the failure only means the collection stays empty
        awaitGeneratorCalled();
        assertThat(flashcardRepository.findAll()).isEmpty();
        assertThat(flashcardCollectionRepository.findById(collectionId)).isPresent();
    }

    @Test
    void givenUserWithoutAiCredits_whenCreateAiFlashcards_thenReturns402AndNothingIsGenerated() {
        // other-user has 0 credits in users-test-data.sql; creating a collection doesn't cost credits
        TokenDto otherUserToken = login("other-user", TEST_USER_PASSWORD);
        Long collectionId = createCollection("no credits", otherUserToken);

        restTestClient.post()
                .uri("/api/v1/flashcards/ai")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRequest(collectionId).build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PAYMENT_REQUIRED)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Out of AI credits");

        Mockito.verifyNoInteractions(flashcardGeneratorService);
        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(collectionId)).isEmpty();
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionOfOtherUser_whenCreateAiFlashcards_thenReturns404AndNothingIsGenerated() {
        // 404 rather than 403 so ids of other users' collections can't be probed
        requestAiFlashcards(aiRequest(OTHER_USER_COLLECTION_ID).build())
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Collection with id 13 not found");

        Mockito.verifyNoInteractions(flashcardGeneratorService);
        assertThat(flashcardRepository.findAllByFlashcardCollectionIdOrderById(OTHER_USER_COLLECTION_ID))
                .as("other-user's collection keeps only its own flashcard")
                .hasSize(1);
    }

    @Test
    void givenNoCollectionExists_whenCreateAiFlashcards_thenReturns404() {
        requestAiFlashcards(aiRequest(999L).build())
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Collection with id 999 not found");

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenTooShortText_whenCreateAiFlashcards_thenReturns400() {
        requestAiFlashcards(aiRequest(1L).textToScan("hola").build())
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {
                                                     },
                        fields -> assertThat(fields).containsExactly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenTooLongText_whenCreateAiFlashcards_thenReturns400() {
        requestAiFlashcards(aiRequest(1L).textToScan("a".repeat(3001)).build())
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {
                                                     },
                        fields -> assertThat(fields).containsExactly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenBlankText_whenCreateAiFlashcards_thenReturns400() {
        requestAiFlashcards(aiRequest(1L).textToScan("            ").build())
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {
                                                     },
                        fields -> assertThat(fields).containsOnly("textToScan"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenNoTargetLanguage_whenCreateAiFlashcards_thenReturns400() {
        requestAiFlashcards(aiRequest(1L).targetLanguage(null).build())
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {
                                                     },
                        fields -> assertThat(fields).containsExactly("targetLanguage"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenNoCollectionId_whenCreateAiFlashcards_thenReturns400() {
        requestAiFlashcards(aiRequest(null).build())
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[*].field").value(new ParameterizedTypeReference<List<String>>() {
                                                     },
                        fields -> assertThat(fields).containsExactly("collectionId"));

        Mockito.verifyNoInteractions(flashcardGeneratorService);
    }

    @Test
    void givenUnknownTargetLanguage_whenCreateAiFlashcards_thenReturns400() {
        authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"collectionId": 1, "textToScan": "hola soy dora", "targetLanguage": "`KLINGON`"}
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
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {
                                           },
                        ids -> assertThat(ids).containsExactly(1L, 2L, 3L))
                .jsonPath("$.content[0].source").isEqualTo("fox")
                .jsonPath("$.content[0].target").isEqualTo("lis")
                .jsonPath("$.totalElements").isEqualTo(3);
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenPageAndSize_whenGetByCollectionId_thenReturnsThatPageInIdOrder() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/flashcards/collections/{collectionId}")
                        .queryParam("page", 1)
                        .queryParam("size", 2)
                        .build(1))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[*].id").value(new ParameterizedTypeReference<List<Long>>() {
                                           },
                        ids -> assertThat(ids).containsExactly(3L))
                .jsonPath("$.totalElements").isEqualTo(3)
                .jsonPath("$.totalPages").isEqualTo(2);
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenTooLargePageSize_whenGetByCollectionId_thenReturns400() {
        authenticatedRestTestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/flashcards/collections/{collectionId}")
                        .queryParam("size", 101)
                        .build(1))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionWithoutFlashcards_whenGetByCollectionId_thenReturnsEmptyPage() {
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", 11)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.totalElements").isEqualTo(0);
    }

    @Test
    @Sql(scripts = "/sql/collections-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void givenCollectionOfOtherUser_whenGetByCollectionId_thenReturns404() {
        // Collection 13 belongs to other-user; 404 rather than 403 so ids of other users' collections can't be probed
        authenticatedRestTestClient().get()
                .uri("/api/v1/flashcards/collections/{collectionId}", OTHER_USER_COLLECTION_ID)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void givenNoToken_whenCreateAiFlashcards_thenReturns401() {
        restTestClient.post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRequest(1L).build())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        Mockito.verifyNoInteractions(flashcardGeneratorService);
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

    // Collections are created on their own endpoint before flashcards are generated into them
    private Long createCollection(String name) {
        return createCollection(name, login(TEST_USER_USERNAME, TEST_USER_PASSWORD));
    }

    private Long createCollection(String name, TokenDto token) {
        restTestClient.post()
                .uri("/api/v1/collections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(CreateCollectionDto.builder().name(name).build())
                .exchange()
                .expectStatus()
                .isCreated();
        return flashcardCollectionRepository.findAll().stream()
                .filter(collection -> collection.getName().equals(name))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    // A valid request for the given collection; tests override single fields to break it
    private static CreateFlashcardsWithAIDto.CreateFlashcardsWithAIDtoBuilder aiRequest(Long collectionId) {
        return CreateFlashcardsWithAIDto.builder()
                .collectionId(collectionId)
                .targetLanguage(TargetLanguage.POLISH)
                .textToScan(TEXT_TO_SCAN);
    }

    private RestTestClient.ResponseSpec requestAiFlashcards(CreateFlashcardsWithAIDto body) {
        return authenticatedRestTestClient().post()
                .uri("/api/v1/flashcards/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    // Generation runs in FlashcardGenerationListener on another thread, after the request has returned
    private void awaitGeneratorCalled() {
        Awaitility.await()
                .atMost(GENERATION_TIMEOUT)
                .untilAsserted(() -> Mockito.verify(flashcardGeneratorService)
                        .generateFlashcards(anyString(), Mockito.any(), anyString()));
    }
}
