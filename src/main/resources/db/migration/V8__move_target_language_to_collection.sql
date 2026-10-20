-- A collection is generated for a single target language, so the language belongs to the collection, not each flashcard.
-- Existing collections take the language of their flashcards; collections without flashcards stay NULL.
ALTER TABLE flashcard_collection
    ADD target_language VARCHAR(20);

UPDATE flashcard_collection collection
SET target_language = (SELECT flashcard.target_language
                       FROM flashcard
                       WHERE flashcard.flashcard_collection_id = collection.id
                         AND flashcard.target_language IS NOT NULL
                       ORDER BY flashcard.id
                       LIMIT 1);

ALTER TABLE flashcard
    DROP COLUMN target_language;
