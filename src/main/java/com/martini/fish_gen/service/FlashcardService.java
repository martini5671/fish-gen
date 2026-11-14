package com.martini.fish_gen.service;

import com.martini.fish_gen.event.FlashcardGenerationRequestedEvent;
import com.martini.fish_gen.exception.OutOfAiCreditsException;
import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateFlashcardsWithAIDto;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final FlashcardCollectionRepository flashcardCollectionRepository;
    private final FlashcardRepository flashcardRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createAiFlashcards(CreateFlashcardsWithAIDto dto) {

        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        currentUserService.getUsername())
                .orElseThrow(EntityNotFoundException::new);

        if (!applicationUser.hasEnoughAiCredits()) throw new OutOfAiCreditsException();

        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findByIdIsAndUser_Username(
                        dto.collectionId(),
                        applicationUser.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Collection with id " + dto.collectionId() + " not found"));

        eventPublisher.publishEvent(
                FlashcardGenerationRequestedEvent.builder()
                        .collectionId(flashcardCollection.getId())
                        .textToScan(dto.textToScan())
                        .targetLanguage(dto.targetLanguage())
                        .username(applicationUser.getUsername())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<Flashcard> getFlashcardsByCollectionId(Long collectionId, int page, int size) {
        if (!flashcardCollectionRepository.existsByIdAndUser_Username(collectionId, currentUserService.getUsername())) {
            throw new EntityNotFoundException("Collection with id " + collectionId + " not found");
        }
        return flashcardRepository.findAllByFlashcardCollectionId(collectionId,
                PageRequest.of(page, size, Sort.by("id")));
    }

}
