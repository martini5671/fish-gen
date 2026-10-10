package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionFilter;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.util.LikePatternUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final FlashcardCollectionRepository flashcardCollectionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<FlashcardCollection> getCollections(CollectionFilter filter, Pageable pageable) {
        return flashcardCollectionRepository.search(
                currentUserService.getUsername(),
                LikePatternUtils.containsIgnoreCase(filter.name()),
                filter.createdFrom(),
                filter.createdTo(),
                pageable);
    }

    @Transactional
    public void removeById(Long id) {
        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findByIdIsAndUser_Username(
                        id, currentUserService.getUsername())
                .orElseThrow(EntityNotFoundException::new);
        flashcardCollectionRepository.delete(flashcardCollection);
    }

    @Transactional
    public void updateCollectionName(Long id, String newCollectionName) {
        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findByIdIsAndUser_Username(
                        id, currentUserService.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Collection with id " + id + " not found"));
        flashcardCollection.setName(newCollectionName);
        flashcardCollectionRepository.save(flashcardCollection);
    }
}
