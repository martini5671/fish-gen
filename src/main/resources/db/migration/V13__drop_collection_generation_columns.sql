-- A collection is now only a named group of flashcards. Generation requests (their text, target language
-- and status) will get their own queue table, so these columns no longer belong on the collection.
ALTER TABLE flashcard_collection
    DROP COLUMN text,
    DROP COLUMN target_language,
    DROP COLUMN generation_status;
