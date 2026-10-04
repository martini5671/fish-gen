-- Removes all data and resets identity sequences, so every test starts from the same ids.
-- flyway_schema_history is deliberately left untouched.
TRUNCATE TABLE flashcard, flashcard_collection, revchanges, revinfo RESTART IDENTITY CASCADE;

ALTER SEQUENCE revinfo_seq RESTART WITH 1;
