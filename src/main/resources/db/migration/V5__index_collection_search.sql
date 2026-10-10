-- Supports the collection search: filter by owner, then range-filter and sort by created_at.
-- created_at is already TIMESTAMP(6) WITHOUT TIME ZONE (microsecond precision), which maps to LocalDateTime.
CREATE INDEX idx_flashcard_collection_user_id_created_at ON flashcard_collection (user_id, created_at);

-- Redundant: user_id is the leading column of the index above, so it still serves the foreign key lookups.
DROP INDEX idx_flashcard_collection_user_id;
