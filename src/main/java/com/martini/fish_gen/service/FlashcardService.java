package com.martini.fish_gen.service;

import com.martini.fish_gen.exception.FlashCardGenerationException;
import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.OriginalText;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardDto;
import com.martini.fish_gen.model.enumeration.Status;
import com.martini.fish_gen.repository.OriginalTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private static final String TARGET_TRANSLATION_TEMPLATE_PARAM = "target_translation";
    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;
    private final OriginalTextRepository originalTextRepository;

    public void createPendingFlashCards(CreateFlashcardsDto dto) {

        String systemPrompt = promptTemplate.render(Map.of(
                TARGET_TRANSLATION_TEMPLATE_PARAM,
                dto.targetLanguage().toString().toLowerCase())
        );

        List<FlashcardDto> generatedFlashCards;
        try {
            generatedFlashCards =
                    Optional.ofNullable(chatClient.prompt()
                                    .system(systemPrompt)
                                    .user(dto.textToScan())
                                    .call()
                                    .entity(new ParameterizedTypeReference<List<FlashcardDto>>() {
                                    }))
                            .orElse(Collections.emptyList());
        } catch (RuntimeException e) {
            throw new FlashCardGenerationException("Ai agent failed to generate flashcards", e);
        }

        OriginalText originalText = OriginalText.builder()
                .text(dto.textToScan())
                .build();

        generatedFlashCards.stream()
                .map(flashCardDto -> Flashcard.builder()
                        .source(flashCardDto.source())
                        .target(flashCardDto.target())
                        .status(Status.PENDING)
                        .targetLanguage(dto.targetLanguage())
                        .build())
                .forEach(originalText::addFlashcard);

        originalTextRepository.save(originalText);

    }
}
