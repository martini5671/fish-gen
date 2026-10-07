package com.martini.fish_gen.service;

import com.martini.fish_gen.exception.FlashCardGenerationException;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
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
public class FlashcardGeneratorService {

    private static final String TARGET_TRANSLATION_TEMPLATE_PARAM = "target_translation";
    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;

    public List<GeneratedFlashcard> generateFlashcards(String userText, TargetLanguage targetLanguage) {
        String systemPrompt = promptTemplate.render(Map.of(
                TARGET_TRANSLATION_TEMPLATE_PARAM,
                targetLanguage.toString().toLowerCase())
        );
        List<GeneratedFlashcard> generatedFlashcards;
        try {
            generatedFlashcards =
                    Optional.ofNullable(chatClient.prompt()
                                    .system(systemPrompt)
                                    .user(userText)
                                    .call()
                                    .entity(new ParameterizedTypeReference<List<GeneratedFlashcard>>() {
                                    }))
                            .orElse(Collections.emptyList());
        } catch (RuntimeException e) {
            throw new FlashCardGenerationException("Ai agent failed to generate flashcards", e);
        }
        return generatedFlashcards;
    }

}
