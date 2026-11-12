package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CreateFlashcardsDto;
import com.martini.fish_gen.model.dto.FlashcardCollectionStatusDto;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionMapperTests {

    private final CollectionMapper mapper = Mappers.getMapper(CollectionMapper.class);

    @Test
    void mapsIdToCollectionIdInStatusDto() {
        FlashcardCollection collection = FlashcardCollection.builder()
                .id(7L)
                .generationStatus(GenerationStatus.COMPLETED)
                .build();

        assertThat(mapper.toStatusDto(collection))
                .isEqualTo(new FlashcardCollectionStatusDto(7L, GenerationStatus.COMPLETED));
    }

    @Test
    void buildsPendingCollectionFromCreateRequest() {
        ApplicationUser user = ApplicationUser.builder().username("anna").build();
        CreateFlashcardsDto dto = new CreateFlashcardsDto("ignored", "Der Hund schläft.", TargetLanguage.values()[0]);

        FlashcardCollection collection = mapper.toNewCollection(dto, "animals", user);

        assertThat(collection.getId()).isNull();
        assertThat(collection.getName()).isEqualTo("animals");
        assertThat(collection.getText()).isEqualTo("Der Hund schläft.");
        assertThat(collection.getTargetLanguage()).isEqualTo(TargetLanguage.values()[0]);
        assertThat(collection.getUser()).isSameAs(user);
        assertThat(collection.getGenerationStatus()).isEqualTo(GenerationStatus.PENDING);
        assertThat(collection.getFlashcards()).isEmpty();
    }
}
