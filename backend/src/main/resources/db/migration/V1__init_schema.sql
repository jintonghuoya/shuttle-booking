CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role ENUM('ROLE_USER', 'ROLE_ORGANIZER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_by BIGINT NOT NULL,
    approved_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (submitted_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_venues_location (latitude, longitude),
    INDEX idx_venues_active (is_active)
);

CREATE TABLE courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    court_number INT NOT NULL,
    name VARCHAR(100),
    price_per_hour_sgd DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    UNIQUE KEY uk_court_venue_number (venue_id, court_number)
);

CREATE TABLE time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'HELD', 'BOOKED') NOT NULL DEFAULT 'AVAILABLE',
    held_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_slot_court_datetime (court_id, slot_date, start_time),
    INDEX idx_slots_date (slot_date),
    INDEX idx_slots_status (status)
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_ref VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    status ENUM('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'REFUNDED', 'EXPIRED') NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (court_id) REFERENCES courts(id),
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id),
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_status (status)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    stripe_session_id VARCHAR(255),
    stripe_payment_intent_id VARCHAR(255),
    stripe_refund_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'SGD',
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE approval_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    review_note TEXT,
    reviewed_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (submitted_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_approvals_status (status)
);
