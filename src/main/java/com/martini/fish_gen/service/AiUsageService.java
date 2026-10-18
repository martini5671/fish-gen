package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiUsageService {

    private final ApplicationUserRepository applicationUserRepository;

    @Transactional
    public void subtractAICredits(long creditsToSubtract, String username) {
        ApplicationUser applicationUser = applicationUserRepository.findByUsername(
                        username)
                .orElseThrow(EntityNotFoundException::new);
        applicationUser.subtractAiCredits(creditsToSubtract);
        applicationUserRepository.save(applicationUser);
    }
}
