-- 12 collections: enough for more than one page with size=5 (pages 0, 1, 2).
-- Names cover case-insensitive matching ("Red" / "red") and literal LIKE wildcards ("%" and "_").
-- created_at values sit on day boundaries in September 2026 to test the createdFrom / createdTo range.
INSERT INTO flashcard_collection (id, name, text, created_at, updated_at)
VALUES (1, 'Red_fox', 'The red fox jumps over the lazy dog.', '2026-09-01 00:00:00', NULL),
       (2, 'red panda', 'Red pandas live in the Himalayas.', '2026-09-02 12:30:00', '2026-09-03 08:00:00'),
       (3, 'whale_red', 'The blue whale is the largest animal.', '2026-09-05 09:15:00', NULL),
       (4, 'Green turtle', 'Green turtles eat seagrass.', '2026-09-10 00:00:00', NULL),
       (5, 'Kitchen vocabulary', 'Knife, fork, spoon, plate, cup.', '2026-09-10 23:59:59', NULL),
       (6, '100% useful phrases', 'Thank you, please, excuse me.', '2026-09-11 00:00:00', NULL),
       (7, 'Travel essentials', 'Where is the train station?', '2026-09-15 14:00:00', '2026-09-16 10:00:00'),
       (8, 'Business English', 'Let us schedule a meeting.', '2026-09-18 08:45:00', NULL),
       (9, 'Polish basics', 'Dzień dobry, dziękuję, proszę.', '2026-09-20 17:20:00', NULL),
       (10, 'Weather words', 'Rain, snow, sunshine, wind.', '2026-09-25 11:11:11', NULL),
       (11, 'Empty collection', 'This collection has no flashcards.', '2026-09-28 06:00:00', NULL),
       (12, 'Animals_and_colors', 'Red fox, blue whale, green turtle.', '2026-09-30 23:59:59', NULL);

INSERT INTO flashcard (id, flashcard_collection_id, target_language, source, target)
VALUES (1, 1, 'POLISH', 'fox', 'lis'),
       (2, 1, 'POLISH', 'red', 'czerwony'),
       (3, 1, 'POLISH', 'lazy dog', 'leniwy pies'),
       (4, 2, 'POLISH', 'panda', 'panda'),
       (5, 2, 'POLISH', 'mountains', 'góry'),
       (6, 3, 'POLISH', 'whale', 'wieloryb'),
       (7, 3, 'POLISH', 'largest', 'największy'),
       (8, 5, 'POLISH', 'knife', 'nóż'),
       (9, 5, 'POLISH', 'fork', 'widelec'),
       (10, 5, 'POLISH', 'spoon', 'łyżka'),
       (11, 5, 'POLISH', 'plate', 'talerz'),
       (12, 9, 'ENGLISH', 'dzień dobry', 'good morning'),
       (13, 9, 'ENGLISH', 'dziękuję', 'thank you'),
       (14, 9, 'ENGLISH', 'proszę', 'please');

-- Rows above use explicit ids, so move the identity sequences past them.
-- Otherwise entities saved during a test would collide with id 1, 2, ...
SELECT setval(pg_get_serial_sequence('flashcard_collection', 'id'), (SELECT MAX(id) FROM flashcard_collection));
SELECT setval(pg_get_serial_sequence('flashcard', 'id'), (SELECT MAX(id) FROM flashcard));
