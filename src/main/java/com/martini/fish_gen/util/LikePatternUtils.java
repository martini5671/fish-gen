package com.martini.fish_gen.util;

// Builds LIKE patterns in Java rather than in the query: a null passed into lower()/concat() fails on PostgreSQL
public final class LikePatternUtils {

    // Must match the ESCAPE clause of the queries using these patterns, e.g. "like :namePattern escape '!'"
    public static final char LIKE_ESCAPE = '!';

    private LikePatternUtils() {
    }

    // Case-insensitive "contains" pattern for comparing against lower(column); null when there is nothing to filter by
    public static String containsIgnoreCase(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + escape(value.trim().toLowerCase()) + "%";
    }

    // Makes %, _ and the escape character itself match literally
    public static String escape(String value) {
        return value
                .replace(String.valueOf(LIKE_ESCAPE), String.valueOf(LIKE_ESCAPE) + LIKE_ESCAPE)
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
    }
}
