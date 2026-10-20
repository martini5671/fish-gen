-- Flashcards are generated asynchronously: a collection is saved as PENDING and later set to COMPLETED or FAILED.
-- Existing collections were generated synchronously, so they default to COMPLETED.
ALTER TABLE flashcard_collection
    ADD generation_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';
