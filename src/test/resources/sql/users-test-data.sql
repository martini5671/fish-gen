-- test-user is the one every integration test logs in as; other-user owns data test-user must never see.
-- Password for both: "password". other-user has no AI credits, for the out-of-credits case.
INSERT INTO users (id, username, password, roles, ai_credits)
VALUES (1, 'test-user', '{bcrypt}$2a$10$AMT4gQdCTHMNKuXEEMscN.PymY1nr02HNA6XWwWxPySO5S7vj599O', 'USER', 1000),
       (2, 'other-user', '{bcrypt}$2a$10$AMT4gQdCTHMNKuXEEMscN.PymY1nr02HNA6XWwWxPySO5S7vj599O', 'USER', 0);

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
