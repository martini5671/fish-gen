-- Deleting a collection removes its flashcards in the database, in the same statement,
-- instead of Hibernate deleting each flashcard row by row
ALTER TABLE flashcard
    DROP CONSTRAINT FK_FLASHCARD_ON_FLASHCARD_COLLECTION;

ALTER TABLE flashcard
    ADD CONSTRAINT FK_FLASHCARD_ON_FLASHCARD_COLLECTION FOREIGN KEY (flashcard_collection_id)
        REFERENCES flashcard_collection (id) ON DELETE CASCADE;
