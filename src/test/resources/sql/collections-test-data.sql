-- 13 collections, 12 owned by test-user: enough for more than one page with size=5 (pages 0, 1, 2).
-- Names cover case-insensitive matching ("Red" / "red") and literal LIKE wildcards ("%" and "_").
-- Rows 1-12 belong to test-user (id 1) from users-test-data.sql; row 13 belongs to other-user (id 2) and matches
-- both the "red" name and the late-September date filters, so it checks that searches are scoped to the owner.
-- created_at values sit on day boundaries in September 2026 to test the createdFrom / createdTo range.
INSERT INTO flashcard_collection (id, name, text, target_language, created_at, updated_at, user_id)
VALUES (1, 'Red_fox', 'The red fox jumps over the lazy dog.', 'POLISH', '2026-09-01 00:00:00', NULL, 1),
       (2, 'red panda', 'Red pandas live in the Himalayas.', 'POLISH', '2026-09-02 12:30:00', '2026-09-03 08:00:00', 1),
       (3, 'whale_red', 'The blue whale is the largest animal.', 'POLISH', '2026-09-05 09:15:00', NULL, 1),
       (4, 'Green turtle', 'Green turtles eat seagrass.', 'POLISH', '2026-09-10 00:00:00', NULL, 1),
       (5, 'Kitchen vocabulary', 'Knife, fork, spoon, plate, cup.', 'POLISH', '2026-09-10 23:59:59', NULL, 1),
       (6, '100% useful phrases', 'Thank you, please, excuse me.', 'POLISH', '2026-09-11 00:00:00', NULL, 1),
       (7, 'Travel essentials', 'Where is the train station?', 'POLISH', '2026-09-15 14:00:00', '2026-09-16 10:00:00', 1),
       (8, 'Business English', 'Let us schedule a meeting.', 'POLISH', '2026-09-18 08:45:00', NULL, 1),
       (9, 'Polish basics', 'Dzień dobry, dziękuję, proszę.', 'ENGLISH', '2026-09-20 17:20:00', NULL, 1),
       (10, 'Weather words', 'Rain, snow, sunshine, wind.', 'POLISH', '2026-09-25 11:11:11', NULL, 1),
       (11, 'Empty collection', 'This collection has no flashcards.', 'POLISH', '2026-09-28 06:00:00', NULL, 1),
       (12, 'Animals_and_colors', 'Red fox, blue whale, green turtle.', 'POLISH', '2026-09-30 23:59:59', NULL, 1),
       (13, 'Red other-user', 'Belongs to other-user, so test-user must never see it.', 'POLISH', '2026-09-29 12:00:00', NULL, 2);

INSERT INTO flashcard (id, flashcard_collection_id, source, target)
VALUES (1, 1, 'fox', 'lis'),
       (2, 1, 'red', 'czerwony'),
       (3, 1, 'lazy dog', 'leniwy pies'),
       (4, 2, 'panda', 'panda'),
       (5, 2, 'mountains', 'góry'),
       (6, 3, 'whale', 'wieloryb'),
       (7, 3, 'largest', 'największy'),
       (8, 5, 'knife', 'nóż'),
       (9, 5, 'fork', 'widelec'),
       (10, 5, 'spoon', 'łyżka'),
       (11, 5, 'plate', 'talerz'),
       (12, 9, 'dzień dobry', 'good morning'),
       (13, 9, 'dziękuję', 'thank you'),
       (14, 9, 'proszę', 'please'),
       (15, 13, 'secret', 'sekret');

-- Rows above use explicit ids, so move the identity sequences past them.
-- Otherwise entities saved during a test would collide with id 1, 2, ...
SELECT setval(pg_get_serial_sequence('flashcard_collection', 'id'), (SELECT MAX(id) FROM flashcard_collection));
SELECT setval(pg_get_serial_sequence('flashcard', 'id'), (SELECT MAX(id) FROM flashcard));
