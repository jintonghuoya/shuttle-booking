package com.shuttlebooking.booking;

import com.shuttlebooking.activity.Activity;
import com.shuttlebooking.activity.ActivityRepository;
import com.shuttlebooking.common.BookingStatus;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.court.Court;
import com.shuttlebooking.court.CourtRepository;
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
    private final CourtRepository courtRepository;
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

        // Derive court from request, time slot, or activity
        Court court;
        if (req.getCourtId() != null) {
            court = courtRepository.findById(req.getCourtId())
                    .orElseThrow(() -> new BusinessException("Court not found"));
            if (slot.getCourt() != null && !slot.getCourt().getId().equals(court.getId())) {
                throw new BusinessException("Time slot does not belong to this court");
            }
        } else if (slot.getCourt() != null) {
            court = slot.getCourt();
        } else {
            throw new BusinessException("No court assigned to this time slot");
        }

        Activity activity = null;
        if (req.getActivityId() != null) {
            activity = activityRepository.findById(req.getActivityId())
                    .orElseThrow(() -> new BusinessException("Activity not found"));

            if (!"PUBLISHED".equals(activity.getStatus())) {
                throw new BusinessException("Activity is not published");
            }

            if (slot.getActivity() == null || !slot.getActivity().getId().equals(activity.getId())) {
                throw new BusinessException("Time slot does not belong to this activity");
            }
        }

        // Hold the slot
        slot.setStatus(SlotStatus.HELD);
        slot.setHeldUntil(Instant.now().plus(HOLD_DURATION));
        timeSlotRepository.save(slot);

        Booking booking = Booking.builder()
                .bookingRef(UUID.randomUUID().toString())
                .user(user)
                .court(court)
                .venue(court.getVenue())
                .timeSlot(slot)
                .activity(activity)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(court.getPricePerHourSgd())
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
