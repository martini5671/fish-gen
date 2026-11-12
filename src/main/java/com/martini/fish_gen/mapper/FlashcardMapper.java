package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dto.FlashcardDto;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface FlashcardMapper {

    FlashcardDto toDto(Flashcard flashcard);

    // The collection is set by FlashcardCollection.addFlashcard, which keeps both sides of the relation in sync
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flashcardCollection", ignore = true)
    Flashcard toFlashcard(GeneratedFlashcard generatedFlashcard);
}
