package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FlashcardCollectionRepository extends JpaRepository<FlashcardCollection, Long>,
        JpaSpecificationExecutor<FlashcardCollection> {
}
