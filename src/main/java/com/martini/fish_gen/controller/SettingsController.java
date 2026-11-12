package com.martini.fish_gen.controller;

import com.martini.fish_gen.mapper.UserSettingsMapper;
import com.martini.fish_gen.model.dto.UserSettingsDto;
import com.martini.fish_gen.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settings")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final UserSettingsMapper userSettingsMapper;

    @Operation(summary = "Get the current user's settings")
    @GetMapping
    public UserSettingsDto getSettings() {
        return userSettingsMapper.toDto(settingsService.get());
    }

    @Operation(summary = "Replace the current user's settings")
    @PostMapping
    public void saveSettings(@RequestBody @Valid UserSettingsDto settings) {
        settingsService.save(userSettingsMapper.toUserSettings(settings));
    }
}
