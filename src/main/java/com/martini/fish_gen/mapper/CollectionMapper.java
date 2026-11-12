package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionDto;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardCollectionStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface CollectionMapper {

    CollectionDto toDto(FlashcardCollection collection);

    @Mapping(target = "collectionId", source = "id")
    FlashcardCollectionStatusDto toStatusDto(FlashcardCollection collection);

    // A new, empty collection waiting for generation. The name is resolved by the caller (it may be generated),
    // and the flashcards are added once generation completes
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "text", source = "dto.textToScan")
    @Mapping(target = "targetLanguage", source = "dto.targetLanguage")
    @Mapping(target = "generationStatus", constant = "PENDING")
    @Mapping(target = "flashcards", ignore = true)
    FlashcardCollection toNewCollection(CreateFlashcardsDto dto, String name, ApplicationUser user);
}
