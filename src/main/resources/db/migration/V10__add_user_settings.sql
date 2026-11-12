-- Per-user preferences (for now the flashcard colours), stored as one JSON document so new settings
-- can be added without a migration each time. Existing users start with no settings, i.e. the defaults.
ALTER TABLE users
    ADD settings JSONB NOT NULL DEFAULT '{}'::jsonb;
