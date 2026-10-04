package com.martini.fish_gen.specification;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FlashcardCollectionSpecification {

    private static final char LIKE_ESCAPE = '!';

    private FlashcardCollectionSpecification() {
    }

    public static Specification<FlashcardCollection> byFilter(CollectionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                String pattern = "%" + escapeLike(filter.name().trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern, LIKE_ESCAPE));
            }
            if (filter.createdFrom() != null) {
                LocalDateTime from = filter.createdFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (filter.createdTo() != null) {
                LocalDateTime toExclusive = filter.createdTo().plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.get("createdAt"), toExclusive));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
