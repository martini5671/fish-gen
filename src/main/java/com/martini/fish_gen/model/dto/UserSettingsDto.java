package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// The backgrounds are optional on save: null resets them to the default colour (see UserSettings).
// The text colours are always sent, and @NotNull marks them as required in the OpenAPI spec.
// Responses always carry all four values
@Getter
@Setter
public class UserSettingsDto {
    @Pattern(regexp = "^#([0-9a-fA-F]{3}){1,2}$")
    String frontBackground;
    @Pattern(regexp = "^#([0-9a-fA-F]{3}){1,2}$")
    String backBackground;
    @NotNull
    @Pattern(regexp = "^(dark|light)$")
    String frontText;
    @NotNull
    @Pattern(regexp = "^(dark|light)$")
    String backText;
}
