CREATE TABLE `organizations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `logo_url` VARCHAR(500),
    `created_by` BIGINT,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`created_by`) REFERENCES `users`(`id`)
) ENGINE=InnoDB;

CREATE TABLE `org_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `org_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) DEFAULT 'MEMBER',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`org_id`) REFERENCES `organizations`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    UNIQUE KEY `uk_org_user` (`org_id`, `user_id`)
) ENGINE=InnoDB;

CREATE TABLE `activities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `org_id` BIGINT NOT NULL,
    `venue_id` BIGINT NOT NULL,
    `court_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `start_hour` INT NOT NULL,
    `end_hour` INT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'PUBLISHED',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`org_id`) REFERENCES `organizations`(`id`),
    FOREIGN KEY (`venue_id`) REFERENCES `venues`(`id`),
    FOREIGN KEY (`court_id`) REFERENCES `courts`(`id`)
) ENGINE=InnoDB;

CREATE TABLE `user_following` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `org_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    FOREIGN KEY (`org_id`) REFERENCES `organizations`(`id`),
    UNIQUE KEY `uk_user_org` (`user_id`, `org_id`)
) ENGINE=InnoDB;

ALTER TABLE `time_slots` ADD COLUMN `activity_id` BIGINT NULL;
ALTER TABLE `time_slots` ADD INDEX `idx_time_slots_activity_id` (`activity_id`);

ALTER TABLE `bookings` ADD COLUMN `activity_id` BIGINT NULL;
