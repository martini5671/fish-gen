-- Settings now always hold concrete values (see UserSettings.defaults()), so the frontend no longer
-- has to fill in missing fields. Keep these values in sync with UserSettings.
--
-- Backfill: the defaults go on the left of ||, so any colour a user already saved wins over them.
-- jsonb_strip_nulls drops explicit nulls first, otherwise a saved null would overwrite the default.
UPDATE users
SET settings = '{"frontBackground": "#dbeafe", "backBackground": "#2563eb", "frontText": "dark", "backText": "light"}'::jsonb
               || jsonb_strip_nulls(settings);

-- Rows inserted outside JPA (e.g. by hand or by later migrations) get the full defaults too, instead of '{}'
ALTER TABLE users
    ALTER COLUMN settings SET DEFAULT
        '{"frontBackground": "#dbeafe", "backBackground": "#2563eb", "frontText": "dark", "backText": "light"}'::jsonb;
