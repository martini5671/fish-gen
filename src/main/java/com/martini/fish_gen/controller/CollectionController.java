package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CollectionDto;
import com.martini.fish_gen.model.dto.CollectionFilter;
import com.martini.fish_gen.model.dto.UpdateCollectionNameDto;
import com.martini.fish_gen.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;

    @GetMapping
    public Page<CollectionDto> getCollections(CollectionFilter collectionFilter,
                                              @PageableDefault(sort = "createdAt",
                                                      direction = Sort.Direction.DESC)
                                              Pageable pageable
    ) {
        return collectionService.getCollections(collectionFilter, pageable)
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
