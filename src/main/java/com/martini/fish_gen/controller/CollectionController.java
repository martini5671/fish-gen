package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.*;
import com.martini.fish_gen.service.CollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collections")
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;

    // page and size are plain params instead of a Pageable, so clients can only sort by a CollectionSort option
    @GetMapping
    public Page<CollectionDto> getCollections(@ParameterObject CollectionFilter collectionFilter,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return collectionService.getCollections(collectionFilter, page, size)
                .map(CollectionDto::from);
    }

    // ids go in the query (?ids=1&ids=2) because browsers and HttpClient drop the body of a GET request
    @GetMapping("/status")
    public List<FlashcardCollectionStatusDto> getCollectionStatus(
            @RequestParam @NotEmpty @Size(max = 100) List<Long> ids) {
        return collectionService.getOwnedCollectionsByIds(ids)
                .stream()
                .map(FlashcardCollectionStatusDto::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        collectionService.removeById(id);
    }

    @PatchMapping("/{id}")
    public void updateCollectionNameById(@PathVariable Long id,
                                         @RequestBody @Valid UpdateCollectionNameDto dto) {
        collectionService.updateCollectionName(id, dto.collectionName());
    }

}
