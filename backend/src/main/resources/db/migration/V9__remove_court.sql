-- V9: Remove court entity, add venue.numberOfCourts, activity.courtDescription, activity.pricePerHourSgd

-- Step 1: Drop FK constraints on court_id (must be before dropping the index)
SET @fk = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'time_slots' AND COLUMN_NAME = 'court_id' AND REFERENCED_TABLE_NAME = 'courts' LIMIT 1);
SET @sql = IF(@fk IS NOT NULL, CONCAT('ALTER TABLE time_slots DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'activities' AND COLUMN_NAME = 'court_id' AND REFERENCED_TABLE_NAME = 'courts' LIMIT 1);
SET @sql = IF(@fk IS NOT NULL, CONCAT('ALTER TABLE activities DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookings' AND COLUMN_NAME = 'court_id' AND REFERENCED_TABLE_NAME = 'courts' LIMIT 1);
SET @sql = IF(@fk IS NOT NULL, CONCAT('ALTER TABLE bookings DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Step 2: Drop unique key (now safe since FK is gone)
ALTER TABLE time_slots DROP INDEX uk_slot_court_datetime;

-- Step 3: Drop court_id columns
ALTER TABLE time_slots DROP COLUMN court_id;
ALTER TABLE activities DROP COLUMN court_id;
ALTER TABLE bookings DROP COLUMN court_id;

-- Step 4: Add new unique key on activity_id
ALTER TABLE time_slots ADD UNIQUE KEY uk_slot_activity_datetime (activity_id, slot_date, start_time);

-- Step 5: Drop tables
DROP TABLE IF EXISTS user_following_court;
DROP TABLE IF EXISTS courts;

-- Step 6: Add new columns
ALTER TABLE venues ADD COLUMN number_of_courts INT;
ALTER TABLE activities ADD COLUMN court_description VARCHAR(255);
ALTER TABLE activities ADD COLUMN price_per_hour_sgd DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Step 7: Backfill activity data from old booking amounts
UPDATE activities a
  JOIN (
    SELECT activity_id, MAX(total_amount) AS amt
    FROM bookings
    GROUP BY activity_id
  ) b ON b.activity_id = a.id
SET a.price_per_hour_sgd = b.amt
WHERE a.price_per_hour_sgd = 0;

-- Step 8: Set court descriptions for seeded activities
UPDATE activities SET court_description = 'Court 1' WHERE id = 1;
UPDATE activities SET court_description = 'Court 5' WHERE id = 2;
UPDATE activities SET court_description = 'Court 1' WHERE id = 3;
UPDATE activities SET court_description = 'Court 1' WHERE id = 4;
UPDATE activities SET court_description = 'Court 1' WHERE id = 5;
UPDATE activities SET court_description = 'Court 1' WHERE id = 6;
UPDATE activities SET court_description = 'Court 1' WHERE id = 7;

-- Step 9: Set venue court counts
UPDATE venues SET number_of_courts = 8 WHERE id = 1;
UPDATE venues SET number_of_courts = 6 WHERE id = 2;
UPDATE venues SET number_of_courts = 6 WHERE id = 3;
UPDATE venues SET number_of_courts = 4 WHERE id = 4;
UPDATE venues SET number_of_courts = 5 WHERE id = 5;
UPDATE venues SET number_of_courts = 6 WHERE id = 6;
