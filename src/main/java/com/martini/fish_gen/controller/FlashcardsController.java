package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/flashcards")
@RequiredArgsConstructor
public class FlashcardsController {
    private final FlashcardService flashCardService;

    @PostMapping("/with-ai")
    public void createWithPrompt(@RequestBody @Valid CreateFlashcardsDto dto) {
        flashCardService.createPendingFlashCards(dto);
    }
}
