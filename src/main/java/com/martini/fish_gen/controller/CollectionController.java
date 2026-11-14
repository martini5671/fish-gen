package com.martini.fish_gen.controller;

import com.martini.fish_gen.mapper.CollectionMapper;
import com.martini.fish_gen.model.dto.*;
import com.martini.fish_gen.service.CollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collections")
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;
    private final CollectionMapper collectionMapper;

    // page and size are plain params instead of a Pageable, so clients can only sort by a CollectionSort option
    @GetMapping
    public Page<CollectionDto> getCollections(@ParameterObject CollectionFilter collectionFilter,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return collectionService.getCollections(collectionFilter, page, size)
                .map(collectionMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCollection(@RequestBody @Valid CreateCollectionDto createCollectionDto) {
        collectionService.saveCollection(createCollectionDto);
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
