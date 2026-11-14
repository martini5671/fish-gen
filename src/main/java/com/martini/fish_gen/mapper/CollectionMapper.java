package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface CollectionMapper {

    CollectionDto toDto(FlashcardCollection collection);
}
