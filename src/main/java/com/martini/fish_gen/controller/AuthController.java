package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.LoginDto;
import com.martini.fish_gen.model.dto.TokenDto;
import com.martini.fish_gen.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Operation(summary = "Log in and obtain a JWT")
    @SecurityRequirements // public endpoint, overrides the global bearer-jwt requirement
    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody LoginDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password()));
        return tokenService.generateToken(authentication);
    }
}
