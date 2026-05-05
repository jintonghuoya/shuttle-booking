-- Default admin user (password: admin123)
-- BCrypt hash for 'admin123'
INSERT INTO users (email, password_hash, name, role, is_active)
VALUES ('admin@shuttlebooking.com', '$2b$10$tJ1TNLXXAw.LDym9RfMdz.YED3o1cLiDDlkwSIHvKtG/N.7cz5RFi', 'Admin', 'ROLE_ADMIN', TRUE);
