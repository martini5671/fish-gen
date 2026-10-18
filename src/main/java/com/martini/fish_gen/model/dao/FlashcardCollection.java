package com.martini.fish_gen.model.dao;

import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlashcardCollection extends AuditingBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TargetLanguage targetLanguage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GenerationStatus generationStatus = GenerationStatus.PENDING;

    @Builder.Default
    @OneToMany(mappedBy = "flashcardCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flashcard> flashcards = new ArrayList<>();

    public void addFlashcard(Flashcard flashcard) {
        flashcards.add(flashcard);
        flashcard.setFlashcardCollection(this);
    }
}
