package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardDto;
import com.martini.fish_gen.service.FlashcardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


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

    // Always sorted by id, so cards keep the order they were generated in; page and size are plain
    // params instead of a Pageable so clients can't pick another sort
    @GetMapping("/collections/{collectionId}")
    public Page<FlashcardDto> getByCollectionId(@PathVariable Long collectionId,
                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return flashCardService.getFlashcardsByCollectionId(collectionId, page, size)
                .map(FlashcardDto::from);
    }

}
