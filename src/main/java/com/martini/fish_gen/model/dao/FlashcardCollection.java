package com.martini.fish_gen.model.dao;

import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    // The foreign key has ON DELETE CASCADE (V9), so deleting a collection leaves the flashcard
    // rows to the database; Hibernate only drops them from the session instead of deleting each one
    @Builder.Default
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "flashcardCollection", cascade = CascadeType.PERSIST)
    private List<Flashcard> flashcards = new ArrayList<>();

    public void addFlashcard(Flashcard flashcard) {
        flashcards.add(flashcard);
        flashcard.setFlashcardCollection(this);
    }
}
