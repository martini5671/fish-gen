package com.martini.fish_gen.mapper;

import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dto.UserTokenUsage;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TokenUsageMapper {

    UserTokenUsage toUserTokenUsage(ApplicationUser user);
}
