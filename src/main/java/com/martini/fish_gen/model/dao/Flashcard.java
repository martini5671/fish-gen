package com.martini.fish_gen.model.dao;

import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_collection_id", nullable = false)
    private FlashcardCollection flashcardCollection;

    @Enumerated(EnumType.STRING)
    private TargetLanguage targetLanguage;

    private String source;
    private String target;

}
