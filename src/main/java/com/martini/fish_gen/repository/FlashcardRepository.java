package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
}
