package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dto.UserSettingsDto;
import com.martini.fish_gen.model.dao.UserSettings;
import org.mapstruct.Mapper;

// The DTO, the stored settings and the frontend settings store share the same field names,
// so every field maps by name
@Mapper(config = MapperConfiguration.class)
public interface UserSettingsMapper {

    UserSettings toUserSettings(UserSettingsDto dto);

    UserSettingsDto toDto(UserSettings settings);
}
