package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.TokenUsage;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.TokenUsageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenUsageService {

    private final ChatModel chatModel;
    private final ApplicationUserRepository applicationUserRepository;
    private final TokenUsageRepository tokenUsageRepository;

    @Transactional
    public void subtractAICredits(long creditsToSubtract, String username) {
        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        username)
                .orElseThrow(EntityNotFoundException::new);
        applicationUser.subtractAiCredits(creditsToSubtract);
        applicationUserRepository.save(applicationUser);
    }

    @Transactional
    public void addTokenUsageEntry(Usage usage, String username) {
        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        username).orElseThrow(EntityNotFoundException::new);
        tokenUsageRepository.save(TokenUsage.builder()
                        .user(applicationUser)
                        .inputTokens(usage.getPromptTokens())
                        .outputTokens(usage.getCompletionTokens())
                        .model(chatModel.getOptions().getModel())
                .build());
    }
}
