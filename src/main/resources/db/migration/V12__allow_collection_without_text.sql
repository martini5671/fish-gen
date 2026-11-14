-- Collections are now created first (POST /api/v1/collections) and filled by AI generation later,
-- so a collection has no source text until generation is requested
ALTER TABLE flashcard_collection
    ALTER COLUMN text DROP NOT NULL;
