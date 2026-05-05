-- Test user (password: user123)
INSERT INTO users (email, password_hash, name, role, is_active)
VALUES ('user@test.com', '$2b$12$jaPmtkC2Lid1rZ.UQA4Q7O32j7QS93U26.TosJ3WDmuCjKJnzRDpO', 'Test User', 'ROLE_USER', TRUE);

-- Test organizer (password: organizer123)
INSERT INTO users (email, password_hash, name, role, is_active)
VALUES ('organizer@test.com', '$2b$12$SJunhb2IquPy6RM6/xHVPeIOsX4VmAXLgERtKRs8I98eeJSREmiAi', 'Test Organizer', 'ROLE_ORGANIZER', TRUE);
