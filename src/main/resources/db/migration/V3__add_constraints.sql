CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255)
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

ALTER TABLE flashcard_collection
    ADD created_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE flashcard_collection
    ADD updated_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE flashcard_collection
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE flashcard
    ADD flashcard_collection_id BIGINT;

ALTER TABLE flashcard
    ALTER COLUMN flashcard_collection_id SET NOT NULL;

ALTER TABLE flashcard
    ADD CONSTRAINT FK_FLASHCARD_ON_FLASHCARD_COLLECTION FOREIGN KEY (flashcard_collection_id) REFERENCES flashcard_collection (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);

ALTER TABLE flashcard_collection
    ALTER COLUMN name TYPE VARCHAR(255) USING (name::VARCHAR(255));

ALTER TABLE flashcard_collection
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE flashcard
    ALTER COLUMN source DROP NOT NULL;

ALTER TABLE flashcard
    ALTER COLUMN target DROP NOT NULL;

ALTER TABLE flashcard
    ALTER COLUMN target_language DROP NOT NULL;
