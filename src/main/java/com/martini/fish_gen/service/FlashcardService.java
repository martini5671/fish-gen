package com.martini.fish_gen.service;

import com.github.javafaker.Faker;
import com.martini.fish_gen.event.FlashcardGenerationRequestedEvent;
import com.martini.fish_gen.exception.OutOfAiCreditsException;
import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final FlashcardCollectionRepository flashcardCollectionRepository;
    private final FlashcardRepository flashcardRepository;
    private final Faker faker = new Faker();
    private final ApplicationUserRepository applicationUserRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    // Saves an empty PENDING collection; FlashcardGenerationListener fills it in once this transaction commits
    @Transactional
    public void createAiFlashcards(CreateFlashcardsDto dto) {
        String resolvedCollectionName = Objects.requireNonNullElse(dto.collectionName(),
                faker.color().name() + "_" + faker.animal().name());

        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        currentUserService.getUsername())
                .orElseThrow(EntityNotFoundException::new);

        // Checked here, on the request thread, so the client gets the error instead of a FAILED collection
        if (!applicationUser.hasEnoughAiCredits()) {
            throw new OutOfAiCreditsException();
        }

        FlashcardCollection flashcardCollection = flashcardCollectionRepository.save(
                FlashcardCollection.builder()
                        .name(resolvedCollectionName)
                        .user(applicationUser)
                        .text(dto.textToScan())
                        .targetLanguage(dto.targetLanguage())
                        .generationStatus(GenerationStatus.PENDING)
                        .build());

        eventPublisher.publishEvent(
                FlashcardGenerationRequestedEvent.builder()
                        .collectionId(flashcardCollection.getId())
                        .textToScan(dto.textToScan())
                        .targetLanguage(dto.targetLanguage())
                        .build());
    }

    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsByCollectionId(Long collectionId) {
        if (!flashcardCollectionRepository.existsByIdAndUser_Username(collectionId, currentUserService.getUsername())) {
            throw new EntityNotFoundException("Collection with id " + collectionId + " not found");
        }
        return flashcardRepository.findAllByFlashcardCollectionIdOrderById(collectionId);
    }

}
