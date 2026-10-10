package com.martini.fish_gen.service;

import com.github.javafaker.Faker;
import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.repository.FlashcardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final FlashcardCollectionRepository flashcardCollectionRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardGeneratorService flashcardGeneratorService;
    private final Faker faker = new Faker();
    private final ApplicationUserRepository applicationUserRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void createAiFlashcards(CreateFlashcardsDto dto) {
        String resolvedCollectionName = Objects.requireNonNullElse(dto.collectionName(),
                faker.color().name() + "_" + faker.animal().name());

        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        currentUserService.getUsername())
                .orElseThrow(EntityNotFoundException::new);

        FlashcardCollection flashcardCollection = FlashcardCollection.builder()
                .name(resolvedCollectionName)
                .user(applicationUser)
                .text(dto.textToScan())
                .build();

        flashcardGeneratorService.generateFlashcards(dto.textToScan(), dto.targetLanguage())
                .stream()
                .map(generatedFlashcard -> Flashcard.builder()
                        .source(generatedFlashcard.source())
                        .target(generatedFlashcard.target())
                        .targetLanguage(dto.targetLanguage())
                        .build())
                .forEach(flashcardCollection::addFlashcard);
        flashcardCollectionRepository.save(flashcardCollection);
    }

    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsByCollectionId(Long collectionId) {
        return flashcardRepository.findAllByFlashcardCollection_IdAndFlashcardCollection_User_UsernameOrderById(
                        collectionId, currentUserService.getUsername())
                .stream()
                .toList();
    }

}
