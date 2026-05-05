-- ============================================================
-- V6: Seed realistic data for shuttle booking app
-- ============================================================

-- ---- Additional Users ----
INSERT INTO users (email, password_hash, name, role, is_active) VALUES
('alice@test.com',   '$2b$12$jaPmtkC2Lid1rZ.UQA4Q7O32j7QS93U26.TosJ3WDmuCjKJnzRDpO', 'Alice Tan',     'ROLE_USER', TRUE),
('bob@test.com',     '$2b$12$jaPmtkC2Lid1rZ.UQA4Q7O32j7QS93U26.TosJ3WDmuCjKJnzRDpO', 'Bob Lim',       'ROLE_USER', TRUE),
('charlie@test.com', '$2b$12$jaPmtkC2Lid1rZ.UQA4Q7O32j7QS93U26.TosJ3WDmuCjKJnzRDpO', 'Charlie Ng',    'ROLE_USER', TRUE),
('diana@test.com',   '$2b$12$jaPmtkC2Lid1rZ.UQA4Q7O32j7QS93U26.TosJ3WDmuCjKJnzRDpO', 'Diana Koh',     'ROLE_USER', TRUE),
(' organizer2@test.com','$2b$12$SJunhb2IquPy6RM6/xHVPeIOsX4VmAXLgERtKRs8I98eeJSREmiAi',' Organizer Two','ROLE_ORGANIZER',TRUE);

-- ---- Venues (real Singapore locations) ----
INSERT INTO venues (name, address, latitude, longitude, description, phone, is_active, submitted_by, approved_by) VALUES
('OCBC Arena - Sport Hub',   '5 Stadium Drive, Singapore 397691',  1.2985, 103.8762, 'Premier badminton facility with 8 courts',    '65123456', TRUE, 1, 1),
('Bishan Sports Hall',        '1 Bishan Street 14, Singapore 579786', 1.3569, 103.8488, 'Public sports hall with 6 courts',            '65234567', TRUE, 1, 1),
('Singapore Badminton Hall',  '21 Woodlands Industrial Street 1, Singapore 757681', 1.4050, 103.7860, 'Dedicated badminton facility',                 '65345678', TRUE, 3, 1),
('Toa Payoh Sports Hall',     '6 Toa Payoh Industrial Park, Singapore 319061', 1.3397, 103.8517, 'Community sports hall with 4 courts',          '65456789', TRUE, 3, 1),
('Our Tampines Hub',          '1 Tampines Walk, Singapore 528523', 1.3532, 103.9444, 'Integrated hub with badminton courts',         '65567890', TRUE, 1, 1),
('Jurong East Sports Centre', '31 Jurong East Street 33, Singapore 609487', 1.3329, 103.7430, 'West side sports centre with 6 courts',       '65678901', TRUE, 3, 1);

-- ---- Courts ----
INSERT INTO courts (venue_id, court_number, name, price_per_hour_sgd, is_active) VALUES
-- OCBC Arena (8 courts)
(1, 1, 'Court 1',  12.00, TRUE), (1, 2, 'Court 2',  12.00, TRUE),
(1, 3, 'Court 3',  12.00, TRUE), (1, 4, 'Court 4',  12.00, TRUE),
(1, 5, 'Court 5',  15.00, TRUE), (1, 6, 'Court 6',  15.00, TRUE),
(1, 7, 'Court 7',  15.00, TRUE), (1, 8, 'Court 8',  15.00, TRUE),
-- Bishan (6 courts)
(2, 1, 'Court 1',  8.00, TRUE),  (2, 2, 'Court 2',  8.00, TRUE),
(2, 3, 'Court 3',  8.00, TRUE),  (2, 4, 'Court 4',  10.00, TRUE),
(2, 5, 'Court 5',  10.00, TRUE), (2, 6, 'Court 6',  10.00, TRUE),
-- SBH (6 courts)
(3, 1, 'Court 1',  9.00, TRUE),  (3, 2, 'Court 2',  9.00, TRUE),
(3, 3, 'Court 3',  9.00, TRUE),  (3, 4, 'Court 4',  11.00, TRUE),
(3, 5, 'Court 5',  11.00, TRUE), (3, 6, 'Court 6',  11.00, TRUE),
-- Toa Payoh (4 courts)
(4, 1, 'Court 1',  7.00, TRUE),  (4, 2, 'Court 2',  7.00, TRUE),
(4, 3, 'Court 3',  9.00, TRUE),  (4, 4, 'Court 4',  9.00, TRUE),
-- Tampines (5 courts)
(5, 1, 'Court 1',  10.00, TRUE), (5, 2, 'Court 2',  10.00, TRUE),
(5, 3, 'Court 3',  10.00, TRUE), (5, 4, 'Court 4',  12.00, TRUE),
(5, 5, 'Court 5',  12.00, TRUE),
-- Jurong East (6 courts)
(6, 1, 'Court 1',  8.00, TRUE),  (6, 2, 'Court 2',  8.00, TRUE),
(6, 3, 'Court 3',  8.00, TRUE),  (6, 4, 'Court 4',  10.00, TRUE),
(6, 5, 'Court 5',  10.00, TRUE), (6, 6, 'Court 6',  10.00, TRUE);

-- ---- Organizations ----
INSERT INTO organizations (name, description, logo_url, created_by, is_active) VALUES
('Shuttle Masters Club',   'Competitive badminton club, weekly training sessions', NULL, 3, TRUE),
('Weekend Warriors',       'Casual weekend badminton for all skill levels',       NULL, 3, TRUE),
('SG Badminton Community', 'Community-driven badminton events and tournaments',    NULL, 8, TRUE);

-- ---- Organization Members ----
INSERT INTO org_members (org_id, user_id, role) VALUES
(1, 3, 'OWNER'), (1, 2, 'MEMBER'),
(2, 3, 'OWNER'), (2, 4, 'MEMBER'), (2, 5, 'MEMBER'),
(3, 8, 'OWNER'), (3, 6, 'MEMBER'), (3, 7, 'MEMBER');

-- ---- User Following ----
INSERT INTO user_following (user_id, org_id) VALUES
(2, 1), (2, 2), (4, 1), (4, 2), (5, 2), (6, 3), (7, 3);

-- ---- Activities ----
INSERT INTO activities (org_id, venue_id, court_id, title, description, start_date, end_date, start_hour, end_hour, status) VALUES
-- Shuttle Masters at OCBC Arena
(1, 1, 1, 'Weekday Evening Training',  'Intensive drills and sparring, intermediate level', '2026-05-05', '2026-05-09', 18, 21, 'PUBLISHED'),
(1, 1, 5, 'Advanced Coaching Session', 'Professional coaching for competitive players',      '2026-05-05', '2026-05-09', 19, 22, 'PUBLISHED'),
-- Weekend Warriors at Bishan
(2, 2, 9, 'Saturday Social Badminton', 'Open play for all levels, just bring your racket!',   '2026-05-10', '2026-05-10', 10, 14, 'PUBLISHED'),
(2, 4, 21, 'Tuesday Night Doubles',    'Fun doubles matches, rotating partners',              '2026-05-06', '2026-05-27', 19, 22, 'PUBLISHED'),
-- SG Badminton Community at SBH & Tampines
(3, 3, 15, 'Community Free Play',      'Free play session for community members',             '2026-05-07', '2026-05-07', 14, 18, 'PUBLISHED'),
(3, 5, 26, 'Tampines Evening Session', 'Weekly evening badminton at Tampines Hub',            '2026-05-06', '2026-05-27', 18, 21, 'PUBLISHED'),
-- Shuttle Masters at Jurong East (weekend)
(1, 6, 31, 'Jurong East Weekend Play', 'Morning badminton in the west',                       '2026-05-10', '2026-05-11', 9, 12, 'PUBLISHED');

-- ---- Time Slots (auto-generated hourly slots for each activity) ----
-- Activity 1: OCBC Court 1, May 5-9, 18:00-21:00 (3 hrs/day x 5 days = 15 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(1,'2026-05-05','18:00','19:00','AVAILABLE',1,NOW()), (1,'2026-05-05','19:00','20:00','BOOKED',1,NOW()), (1,'2026-05-05','20:00','21:00','AVAILABLE',1,NOW()),
(1,'2026-05-06','18:00','19:00','AVAILABLE',1,NOW()), (1,'2026-05-06','19:00','20:00','AVAILABLE',1,NOW()), (1,'2026-05-06','20:00','21:00','HELD',1,NOW()),
(1,'2026-05-07','18:00','19:00','BOOKED',1,NOW()),    (1,'2026-05-07','19:00','20:00','BOOKED',1,NOW()),    (1,'2026-05-07','20:00','21:00','AVAILABLE',1,NOW()),
(1,'2026-05-08','18:00','19:00','AVAILABLE',1,NOW()), (1,'2026-05-08','19:00','20:00','AVAILABLE',1,NOW()), (1,'2026-05-08','20:00','21:00','AVAILABLE',1,NOW()),
(1,'2026-05-09','18:00','19:00','AVAILABLE',1,NOW()), (1,'2026-05-09','19:00','20:00','BOOKED',1,NOW()),    (1,'2026-05-09','20:00','21:00','AVAILABLE',1,NOW());

-- Activity 2: OCBC Court 5, May 5-9, 19:00-22:00 (3 hrs/day x 5 days = 15 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(5,'2026-05-05','19:00','20:00','AVAILABLE',2,NOW()), (5,'2026-05-05','20:00','21:00','BOOKED',2,NOW()), (5,'2026-05-05','21:00','22:00','AVAILABLE',2,NOW()),
(5,'2026-05-06','19:00','20:00','BOOKED',2,NOW()),    (5,'2026-05-06','20:00','21:00','AVAILABLE',2,NOW()), (5,'2026-05-06','21:00','22:00','AVAILABLE',2,NOW()),
(5,'2026-05-07','19:00','20:00','AVAILABLE',2,NOW()), (5,'2026-05-07','20:00','21:00','HELD',2,NOW()),    (5,'2026-05-07','21:00','22:00','AVAILABLE',2,NOW()),
(5,'2026-05-08','19:00','20:00','AVAILABLE',2,NOW()), (5,'2026-05-08','20:00','21:00','AVAILABLE',2,NOW()), (5,'2026-05-08','21:00','22:00','BOOKED',2,NOW()),
(5,'2026-05-09','19:00','20:00','AVAILABLE',2,NOW()), (5,'2026-05-09','20:00','21:00','AVAILABLE',2,NOW()), (5,'2026-05-09','21:00','22:00','AVAILABLE',2,NOW());

-- Activity 3: Bishan Court 1, May 10, 10:00-14:00 (4 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(9,'2026-05-10','10:00','11:00','AVAILABLE',3,NOW()), (9,'2026-05-10','11:00','12:00','BOOKED',3,NOW()),
(9,'2026-05-10','12:00','13:00','AVAILABLE',3,NOW()), (9,'2026-05-10','13:00','14:00','AVAILABLE',3,NOW());

-- Activity 4: Toa Payoh Court 1, May 6/13/20/27, 19:00-22:00 (3 slots x 4 days = 12 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(21,'2026-05-06','19:00','20:00','BOOKED',4,NOW()),   (21,'2026-05-06','20:00','21:00','AVAILABLE',4,NOW()), (21,'2026-05-06','21:00','22:00','AVAILABLE',4,NOW()),
(21,'2026-05-13','19:00','20:00','AVAILABLE',4,NOW()), (21,'2026-05-13','20:00','21:00','BOOKED',4,NOW()),    (21,'2026-05-13','21:00','22:00','AVAILABLE',4,NOW()),
(21,'2026-05-20','19:00','20:00','AVAILABLE',4,NOW()), (21,'2026-05-20','20:00','21:00','AVAILABLE',4,NOW()), (21,'2026-05-20','21:00','22:00','HELD',4,NOW()),
(21,'2026-05-27','19:00','20:00','AVAILABLE',4,NOW()), (21,'2026-05-27','20:00','21:00','AVAILABLE',4,NOW()), (21,'2026-05-27','21:00','22:00','AVAILABLE',4,NOW());

-- Activity 5: SBH Court 1, May 7, 14:00-18:00 (4 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(15,'2026-05-07','14:00','15:00','AVAILABLE',5,NOW()), (15,'2026-05-07','15:00','16:00','BOOKED',5,NOW()),
(15,'2026-05-07','16:00','17:00','AVAILABLE',5,NOW()), (15,'2026-05-07','17:00','18:00','AVAILABLE',5,NOW());

-- Activity 6: Tampines Court 1, May 6-27 weekly, 18:00-21:00 (3 slots x 4 weeks = 12 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(26,'2026-05-06','18:00','19:00','AVAILABLE',6,NOW()), (26,'2026-05-06','19:00','20:00','BOOKED',6,NOW()),    (26,'2026-05-06','20:00','21:00','AVAILABLE',6,NOW()),
(26,'2026-05-13','18:00','19:00','BOOKED',6,NOW()),    (26,'2026-05-13','19:00','20:00','AVAILABLE',6,NOW()), (26,'2026-05-13','20:00','21:00','AVAILABLE',6,NOW()),
(26,'2026-05-20','18:00','19:00','AVAILABLE',6,NOW()), (26,'2026-05-20','19:00','20:00','HELD',6,NOW()),    (26,'2026-05-20','20:00','21:00','AVAILABLE',6,NOW()),
(26,'2026-05-27','18:00','19:00','AVAILABLE',6,NOW()), (26,'2026-05-27','19:00','20:00','AVAILABLE',6,NOW()), (26,'2026-05-27','20:00','21:00','BOOKED',6,NOW());

-- Activity 7: Jurong East Court 1, May 10-11, 9:00-12:00 (3 slots x 2 days = 6 slots)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, activity_id, created_at) VALUES
(31,'2026-05-10','09:00','10:00','AVAILABLE',7,NOW()), (31,'2026-05-10','10:00','11:00','BOOKED',7,NOW()), (31,'2026-05-10','11:00','12:00','AVAILABLE',7,NOW()),
(31,'2026-05-11','09:00','10:00','AVAILABLE',7,NOW()), (31,'2026-05-11','10:00','11:00','AVAILABLE',7,NOW()), (31,'2026-05-11','11:00','12:00','HELD',7,NOW());

-- ---- Bookings (existing confirmed/pending bookings) ----
INSERT INTO bookings (booking_ref, user_id, court_id, venue_id, time_slot_id, activity_id, status, total_amount, created_at) VALUES
('BK-2026-0505-001', 2, 1, 1, 2,  1, 'CONFIRMED',  12.00, '2026-05-04 10:00:00'),
('BK-2026-0505-002', 4, 1, 1, 7,  1, 'CONFIRMED',  12.00, '2026-05-04 11:30:00'),
('BK-2026-0505-003', 5, 1, 1, 8,  1, 'CONFIRMED',  12.00, '2026-05-04 12:00:00'),
('BK-2026-0505-004', 6, 1, 1, 14, 1, 'CONFIRMED',  12.00, '2026-05-04 14:00:00'),
('BK-2026-0505-005', 2, 5, 1, 17, 2, 'CONFIRMED',  15.00, '2026-05-04 15:00:00'),
('BK-2026-0505-006', 4, 5, 1, 22, 2, 'CONFIRMED',  15.00, '2026-05-04 16:00:00'),
('BK-2026-0505-007', 7, 5, 1, 29, 2, 'CONFIRMED',  15.00, '2026-05-04 17:00:00'),
('BK-2026-0505-008', 6, 9, 2, 34, 3, 'CONFIRMED',   8.00, '2026-05-04 18:00:00'),
('BK-2026-0505-009', 2, 21,4, 40, 4, 'CONFIRMED',   7.00, '2026-05-04 19:00:00'),
('BK-2026-0505-010', 5, 21,4, 44, 4, 'CONFIRMED',   7.00, '2026-05-04 20:00:00'),
('BK-2026-0505-011', 7, 15,3, 49, 5, 'CONFIRMED',   9.00, '2026-05-05 08:00:00'),
('BK-2026-0505-012', 6, 26,5, 53, 6, 'CONFIRMED',  10.00, '2026-05-05 09:00:00'),
('BK-2026-0505-013', 4, 26,5, 57, 6, 'CONFIRMED',  10.00, '2026-05-05 10:00:00'),
('BK-2026-0505-014', 2, 31,6, 65, 7, 'CONFIRMED',   8.00, '2026-05-05 11:00:00'),
('BK-2026-0505-015', 5, 1, 1, 6,  1, 'PENDING_PAYMENT', 12.00, '2026-05-05 12:00:00'),
-- Cancelled booking
('BK-2026-0505-016', 4, 1, 1, 1,  1, 'CANCELLED',  12.00, '2026-05-03 09:00:00');

-- ---- Payments for confirmed bookings ----
INSERT INTO payments (booking_id, stripe_session_id, amount, currency, status, created_at) VALUES
(1,  'sess_mock_001', 12.00, 'SGD', 'COMPLETED', '2026-05-04 10:01:00'),
(2,  'sess_mock_002', 12.00, 'SGD', 'COMPLETED', '2026-05-04 11:31:00'),
(3,  'sess_mock_003', 12.00, 'SGD', 'COMPLETED', '2026-05-04 12:01:00'),
(4,  'sess_mock_004', 12.00, 'SGD', 'COMPLETED', '2026-05-04 14:01:00'),
(5,  'sess_mock_005', 15.00, 'SGD', 'COMPLETED', '2026-05-04 15:01:00'),
(6,  'sess_mock_006', 15.00, 'SGD', 'COMPLETED', '2026-05-04 16:01:00'),
(7,  'sess_mock_007', 15.00, 'SGD', 'COMPLETED', '2026-05-04 17:01:00'),
(8,  'sess_mock_008',  8.00, 'SGD', 'COMPLETED', '2026-05-04 18:01:00'),
(9,  'sess_mock_009',  7.00, 'SGD', 'COMPLETED', '2026-05-04 19:01:00'),
(10, 'sess_mock_010',  7.00, 'SGD', 'COMPLETED', '2026-05-04 20:01:00'),
(11, 'sess_mock_011',  9.00, 'SGD', 'COMPLETED', '2026-05-05 08:01:00'),
(12, 'sess_mock_012', 10.00, 'SGD', 'COMPLETED', '2026-05-05 09:01:00'),
(13, 'sess_mock_013', 10.00, 'SGD', 'COMPLETED', '2026-05-05 10:01:00'),
(14, 'sess_mock_014',  8.00, 'SGD', 'COMPLETED', '2026-05-05 11:01:00'),
(16, 'sess_mock_016', 12.00, 'SGD', 'REFUNDED',  '2026-05-03 09:05:00');
