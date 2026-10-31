package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlashcardCollectionRepository extends JpaRepository<FlashcardCollection, Long> {

    Optional<FlashcardCollection> findByIdIsAndUser_Username(Long id, String userUsername);

    boolean existsByIdAndUser_Username(Long id, String userUsername);

    List<FlashcardCollection> findAllByIdInAndUser_Username(Collection<Long> ids, String userUsername);

    // A null namePattern disables the name filter; it is a ready LIKE pattern with '!' as the escape character.
    @Query("""
            select c from FlashcardCollection c
            where c.user.username = :username
              and (:namePattern is null or lower(c.name) like :namePattern escape '!')
            """)
    Page<FlashcardCollection> search(String username,
                                     String namePattern,
                                     Pageable pageable);
}
