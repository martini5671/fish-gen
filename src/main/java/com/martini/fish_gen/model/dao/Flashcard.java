package com.martini.fish_gen.model.dao;

import com.martini.fish_gen.model.enumeration.Status;
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
    @JoinColumn(name = "original_text_id", nullable = false)
    private OriginalText originalText;

    @Enumerated(EnumType.STRING)
    private TargetLanguage targetLanguage;

    @Enumerated(EnumType.STRING)
    private Status status;
    private String source;
    private String target;

}
