package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.UserSettings;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Settings always belong to the caller: the user is looked up from the JWT, never from the request
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final CurrentUserService currentUserService;
    private final ApplicationUserRepository applicationUserRepository;

    @Transactional(readOnly = true)
    public UserSettings get() {
        return getCurrentUser().getSettings();
    }

    @Transactional
    public void save(UserSettings settings) {
        getCurrentUser().setSettings(settings);
    }

    private ApplicationUser getCurrentUser() {
        return applicationUserRepository.findByUsername(currentUserService.getUsername())
                .orElseThrow(EntityNotFoundException::new);
    }
}
