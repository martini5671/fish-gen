package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findAllByFlashcardCollectionIdOrderById(Long flashcardCollectionId);

    List<Flashcard> findAllByFlashcardCollection_IdAndFlashcardCollection_User_UsernameOrderById(Long flashcardCollectionId, String ownerUsername);

}
