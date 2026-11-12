package com.martini.fish_gen.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

// Shared by every mapper: Spring beans, and an unmapped target field fails the build, so a field added to an
// entity or DTO can't be silently left out of the mapping
@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapperConfiguration {
}
