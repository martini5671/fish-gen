package com.martini.fish_gen.configuration;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.enumeration.Role;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.TokenUsageRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "koczot.martini@gmail.com";

    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserRepository applicationUserRepository;

    @Value("${admin.password}")
    private String adminPassword;

    @Transactional
    @Override
    public void run(String @NonNull ... args) throws Exception {
        if (applicationUserRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            return;
        }
        ApplicationUser user = ApplicationUser.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Role.ADMIN.name())
                .aiCredits(10000000L)
                .build();
        user.addRole(Role.ADMIN);
        applicationUserRepository.save(user);
    }
}
