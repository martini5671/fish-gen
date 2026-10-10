package com.martini.fish_gen.service;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

// Reads the authenticated user from the current thread's SecurityContext, so it only works on the request thread
@Service
public class CurrentUserService {

    // The subject is the username, set by TokenService when the token was issued
    public String getUsername() {
        return getJwt().getSubject();
    }

    public Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new AuthenticationCredentialsNotFoundException("No JWT found in the security context");
    }
}
