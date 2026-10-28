package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.enumeration.CollectionSort;

// e.g. ?name=animals&sort=NAME_ASC; a missing sort means NEWEST
public record CollectionFilter(
        String name,
        CollectionSort sort
) {
    public CollectionSort sortOrDefault() {
        return sort != null ? sort : CollectionSort.NEWEST;
    }
}
