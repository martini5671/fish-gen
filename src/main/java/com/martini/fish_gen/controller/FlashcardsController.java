package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardDto;
import com.martini.fish_gen.service.FlashcardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Flashcards")
@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardsController {
    private final FlashcardService flashCardService;

    // 202: the collection is saved as PENDING and filled in asynchronously; poll its generationStatus
    @PostMapping("/ai")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createAiFlashcards(@RequestBody @Valid CreateFlashcardsDto dto) {
        flashCardService.createAiFlashcards(dto);
    }

    @GetMapping("/collections/{collectionId}")
    public List<FlashcardDto> getByCollectionId(@PathVariable Long collectionId) {
        return flashCardService.getFlashcardsByCollectionId(collectionId)
                .stream()
                .map(FlashcardDto::from)
                .toList();
    }

}
