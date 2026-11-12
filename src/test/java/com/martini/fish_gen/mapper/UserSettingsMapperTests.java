package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dto.UserSettingsDto;
import com.martini.fish_gen.model.dao.UserSettings;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserSettingsMapperTests {

    private final UserSettingsMapper mapper = Mappers.getMapper(UserSettingsMapper.class);

    @Test
    void mapsDtoToUserSettings() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setFrontBackground("#dbeafe");
        dto.setBackBackground("#2563eb");
        dto.setFrontText("dark");
        dto.setBackText("light");

        UserSettings settings = mapper.toUserSettings(dto);

        assertThat(settings).isEqualTo(new UserSettings("#dbeafe", "#2563eb", "dark", "light"));
    }

    @Test
    void mapsUserSettingsToDto() {
        UserSettingsDto dto = mapper.toDto(new UserSettings("#dbeafe", "#2563eb", "dark", "light"));

        assertThat(dto.getFrontBackground()).isEqualTo("#dbeafe");
        assertThat(dto.getBackBackground()).isEqualTo("#2563eb");
        assertThat(dto.getFrontText()).isEqualTo("dark");
        assertThat(dto.getBackText()).isEqualTo("light");
    }

    @Test
    void fillsUnsetColoursWithDefaults() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setFrontText("light");
        dto.setBackText("dark");

        UserSettings settings = mapper.toUserSettings(dto);

        assertThat(settings).isEqualTo(new UserSettings("#dbeafe", "#2563eb", "light", "dark"));
    }

    @Test
    void mapsNullToNull() {
        assertThat(mapper.toUserSettings(null)).isNull();
        assertThat(mapper.toDto(null)).isNull();
    }
}
