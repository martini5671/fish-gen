package com.martini.fish_gen.model.enumeration;

import org.springframework.data.domain.Sort;

// The id tie-breaker keeps the order stable across pages when two collections share a date or name
public enum CollectionSort {
    NEWEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    OLDEST(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))),
    NAME_ASC(Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("id")));

    private final Sort sort;

    CollectionSort(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }
}
