package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlashcardCollectionRepository extends JpaRepository<FlashcardCollection, Long> {

    Optional<FlashcardCollection> findByIdIsAndUser_Username(Long id, String userUsername);

    boolean existsByIdAndUser_Username(Long id, String userUsername);

    // A null parameter disables its filter; namePattern is a ready LIKE pattern with '!' as the escape character.
    // The casts give PostgreSQL a type for a null timestamp, otherwise it fails with "could not determine data type".
    @Query("""
            select c from FlashcardCollection c
            where c.user.username = :username
              and (:namePattern is null or lower(c.name) like :namePattern escape '!')
              and (cast(:createdFrom as LocalDateTime) is null or c.createdAt >= :createdFrom)
              and (cast(:createdTo as LocalDateTime) is null or c.createdAt <= :createdTo)
            """)
    Page<FlashcardCollection> search(String username,
                                     String namePattern,
                                     LocalDateTime createdFrom,
                                     LocalDateTime createdTo,
                                     Pageable pageable);
}
