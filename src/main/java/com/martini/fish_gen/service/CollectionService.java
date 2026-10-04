package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionFilter;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.specification.FlashcardCollectionSpecification;
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

    @Transactional(readOnly = true)
    public Page<FlashcardCollection> getCollections(CollectionFilter filter, Pageable pageable) {
        return flashcardCollectionRepository.findAll(FlashcardCollectionSpecification.byFilter(filter), pageable);
    }

    public void removeById(Long id) {
        flashcardCollectionRepository.deleteById(id);
    }

    public void updateCollectionName(Long id, String newCollectionName) {
        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection with id " + id + " not found"));
        flashcardCollection.setName(newCollectionName);
        flashcardCollectionRepository.save(flashcardCollection);
    }
}
