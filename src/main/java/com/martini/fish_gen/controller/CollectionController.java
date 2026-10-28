package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CollectionDto;
import com.martini.fish_gen.model.dto.CollectionFilter;
import com.martini.fish_gen.model.dto.UpdateCollectionNameDto;
import com.martini.fish_gen.service.CollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        collectionService.removeById(id);
    }

    @PatchMapping("/{id}")
    public void updateCollectionNameById(@PathVariable Long id,
                                         @RequestBody UpdateCollectionNameDto dto) {
        collectionService.updateCollectionName(id, dto.collectionName());
    }

}
