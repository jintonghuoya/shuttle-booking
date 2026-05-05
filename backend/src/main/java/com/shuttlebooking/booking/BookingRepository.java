package com.shuttlebooking.booking;

import com.shuttlebooking.common.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingRef(String bookingRef);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByStatus(BookingStatus status);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
