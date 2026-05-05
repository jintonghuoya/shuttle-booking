package com.shuttlebooking.booking;

import com.shuttlebooking.activity.Activity;
import com.shuttlebooking.activity.ActivityRepository;
import com.shuttlebooking.common.BookingStatus;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.payment.PaymentService;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.user.User;
import com.shuttlebooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final VenueRepository venueRepository;
    private final ActivityRepository activityRepository;
    private final PaymentService paymentService;

    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);

    @Transactional
    public Booking createBooking(BookingRequest req, User user) {
        TimeSlot slot = timeSlotRepository.findByIdWithLock(req.getTimeSlotId())
                .orElseThrow(() -> new BusinessException("Time slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BusinessException("Time slot is not available");
        }

        Activity activity = null;
        if (slot.getActivity() != null) {
            activity = slot.getActivity();
        } else if (req.getActivityId() != null) {
            activity = activityRepository.findById(req.getActivityId())
                    .orElseThrow(() -> new BusinessException("Activity not found"));
            if (!"PUBLISHED".equals(activity.getStatus())) {
                throw new BusinessException("Activity is not published");
            }
        }

        if (activity == null) {
            throw new BusinessException("Time slot is not associated with an activity");
        }

        // Hold the slot
        slot.setStatus(SlotStatus.HELD);
        slot.setHeldUntil(Instant.now().plus(HOLD_DURATION));
        timeSlotRepository.save(slot);

        Booking booking = Booking.builder()
                .bookingRef(UUID.randomUUID().toString())
                .user(user)
                .venue(activity.getVenue())
                .timeSlot(slot)
                .activity(activity)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(activity.getPricePerHourSgd())
                .build();

        return bookingRepository.save(booking);
    }

    public List<BookingResponse> getMyBookings(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse getBooking(Long id, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException("Booking not found"));
        return BookingResponse.from(booking);
    }

    @Transactional
    public void cancelBooking(Long id, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED ||
            booking.getStatus() == BookingStatus.REFUNDED ||
            booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("Booking is already " + booking.getStatus());
        }

        boolean wasPaid = booking.getStatus() == BookingStatus.CONFIRMED;

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (wasPaid) {
            try {
                paymentService.processRefund(booking);
            } catch (Exception e) {
                throw new BusinessException("Cancellation succeeded but refund failed: " + e.getMessage());
            }
        }

        // Release the slot
        TimeSlot slot = booking.getTimeSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setHeldUntil(null);
        timeSlotRepository.save(slot);
    }

    public Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHeldSlots() {
        int expired = timeSlotRepository.expireHeldSlots(Instant.now());
        if (expired > 0) {
            List<Booking> pendingBookings = bookingRepository.findByStatus(BookingStatus.PENDING_PAYMENT);
            for (Booking booking : pendingBookings) {
                if (booking.getTimeSlot().getStatus() == SlotStatus.AVAILABLE) {
                    booking.setStatus(BookingStatus.EXPIRED);
                    bookingRepository.save(booking);
                }
            }
        }
    }
}
