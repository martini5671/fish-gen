package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {

    private final ApplicationUserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        ApplicationUser applicationUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.builder()
                .username(applicationUser.getUsername())
                .password(applicationUser.getPassword())
                .roles(applicationUser.getRoles().stream()
                        .map(Enum::name)
                        .distinct()
                        .toArray(String[]::new))
                .build();
    }
}
