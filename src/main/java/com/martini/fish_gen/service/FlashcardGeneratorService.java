package com.martini.fish_gen.service;

import com.martini.fish_gen.exception.FlashCardGenerationException;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class FlashcardGeneratorService {

    private static final String TARGET_TRANSLATION_TEMPLATE_PARAM = "target_translation";
    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;
    private final CurrentUserService currentUserService;
    private final TokenUsageService aiUsageService;

    @Transactional
    public List<GeneratedFlashcard> generateFlashcards(String userText, TargetLanguage targetLanguage) {
        String systemPrompt = getSystemPrompt(targetLanguage);
        ResponseEntity<ChatResponse, List<GeneratedFlashcard>> chatResponse;
        try {
            chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userText)
                    .call()
                    .responseEntity(new ParameterizedTypeReference<>() {
                    });

        } catch (RuntimeException e) {
            throw new FlashCardGenerationException("Ai agent failed to generate flashcards", e);
        }

        if (Objects.isNull(chatResponse.getEntity())) {
            throw new FlashCardGenerationException("Ai agent did not generate any flashcards");
        }
        Usage usage = Objects.requireNonNull(chatResponse.getResponse()).getMetadata().getUsage();
        aiUsageService.subtractAICredits(usage.getTotalTokens(), currentUserService.getUsername());
        aiUsageService.addTokenUsageEntry(usage, currentUserService.getUsername());
        return chatResponse.getEntity();
    }

    private @NonNull String getSystemPrompt(TargetLanguage targetLanguage) {
        return promptTemplate.render(Map.of(
                TARGET_TRANSLATION_TEMPLATE_PARAM,
                targetLanguage.toString().toLowerCase())
        );
    }


}
