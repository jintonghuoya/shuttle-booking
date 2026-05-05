CREATE TABLE `user_following_court` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `court_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    FOREIGN KEY (`court_id`) REFERENCES `courts`(`id`),
    UNIQUE KEY `uk_user_court` (`user_id`, `court_id`)
) ENGINE=InnoDB;
