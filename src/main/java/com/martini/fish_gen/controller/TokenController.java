package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.TokenUsageDto;
import com.martini.fish_gen.model.dto.UserTokenUsage;
import com.martini.fish_gen.service.TokenUsageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usage")
@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class TokenController {

    private final TokenUsageService tokenUsageService;

    @GetMapping
    public UserTokenUsage getTokenUsage(){
        return tokenUsageService.getTokenUsage();
    }

}
