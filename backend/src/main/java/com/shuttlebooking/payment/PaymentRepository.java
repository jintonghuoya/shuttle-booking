package com.shuttlebooking.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByStripeSessionId(String sessionId);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}
