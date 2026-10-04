package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardDto;
import com.martini.fish_gen.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardsController {
    private final FlashcardService flashCardService;

    @PostMapping("/with-prompt")
    public void createWithPrompt(@RequestBody @Valid CreateFlashcardsDto dto) {
        flashCardService.createPendingFlashCards(dto);
    }

    @GetMapping("/collections/{collectionId}")
    public List<FlashcardDto> getByCollectionId(@PathVariable Long collectionId) {
        return flashCardService.getFlashcardsByCollectionId(collectionId);
    }

}
