package com.martini.fish_gen.model.dao;

import java.util.Objects;

/**
 * Per-user preferences, stored as JSON in {@code users.settings}.
 * Every field always holds a concrete value: a missing or null one (e.g. from an older JSON document,
 * or a background the user reset) falls back to the default, so the frontend never has to fill gaps.
 *
 * @param frontBackground CSS colour of the flashcard front, e.g. {@code #dbeafe}
 * @param backBackground  CSS colour of the flashcard back
 * @param frontText       text colour on the front: {@code dark} or {@code light}
 * @param backText        text colour on the back: {@code dark} or {@code light}
 */
public record UserSettings(
        String frontBackground,
        String backBackground,
        String frontText,
        String backText
) {

    // Same as --fg-blue-100 / --fg-blue-600 in the frontend's styles.scss.
    // Keep in sync with V11__backfill_user_settings_defaults.sql
    public static final String DEFAULT_FRONT_BACKGROUND = "#dbeafe";
    public static final String DEFAULT_BACK_BACKGROUND = "#2563eb";
    public static final String DEFAULT_FRONT_TEXT = "dark";
    public static final String DEFAULT_BACK_TEXT = "light";

    public UserSettings {
        frontBackground = Objects.requireNonNullElse(frontBackground, DEFAULT_FRONT_BACKGROUND);
        backBackground = Objects.requireNonNullElse(backBackground, DEFAULT_BACK_BACKGROUND);
        frontText = Objects.requireNonNullElse(frontText, DEFAULT_FRONT_TEXT);
        backText = Objects.requireNonNullElse(backText, DEFAULT_BACK_TEXT);
    }

    public static UserSettings defaults() {
        return new UserSettings(DEFAULT_FRONT_BACKGROUND, DEFAULT_BACK_BACKGROUND, DEFAULT_FRONT_TEXT, DEFAULT_BACK_TEXT);
    }
}
