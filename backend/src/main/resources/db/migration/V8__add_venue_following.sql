CREATE TABLE `user_following_venue` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `venue_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    FOREIGN KEY (`venue_id`) REFERENCES `venues`(`id`),
    UNIQUE KEY `uk_user_venue` (`user_id`, `venue_id`)
) ENGINE=InnoDB;
