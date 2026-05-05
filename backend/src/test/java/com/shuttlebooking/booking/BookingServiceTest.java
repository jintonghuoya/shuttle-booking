package com.shuttlebooking.booking;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.BookingStatus;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.activity.Activity;
import com.shuttlebooking.activity.ActivityRepository;
import com.shuttlebooking.payment.PaymentService;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.user.User;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Activity activity;
    private TimeSlot slot;
    private Venue venue;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
        venue = Venue.builder().id(1L).name("Test Venue").active(true).build();
        activity = Activity.builder().id(1L).venue(venue).pricePerHourSgd(new BigDecimal("10.00")).status("PUBLISHED").build();
        slot = TimeSlot.builder().id(1L).activity(activity).slotDate(LocalDate.now()).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).status(SlotStatus.AVAILABLE).build();
    }

    @Test
    void createBooking_success() {
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(slot));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(1L);

        Booking result = bookingService.createBooking(req, user);

        assertNotNull(result);
        assertEquals(BookingStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(new BigDecimal("10.00"), result.getTotalAmount());
        assertEquals(SlotStatus.HELD, slot.getStatus());
        assertNotNull(slot.getHeldUntil());
        verify(timeSlotRepository).save(slot);
        verify(bookingRepository).save(any());
    }

    @Test
    void createBooking_slotNotAvailable_throwsException() {
        slot.setStatus(SlotStatus.BOOKED);
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(slot));

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(1L);

        assertThrows(BusinessException.class, () -> bookingService.createBooking(req, user));
    }

    @Test
    void createBooking_noActivity_throwsException() {
        TimeSlot noActivitySlot = TimeSlot.builder().id(2L).activity(null).slotDate(LocalDate.now()).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).status(SlotStatus.AVAILABLE).build();
        when(timeSlotRepository.findByIdWithLock(2L)).thenReturn(Optional.of(noActivitySlot));

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(2L);

        assertThrows(BusinessException.class, () -> bookingService.createBooking(req, user));
    }

    @Test
    void cancelBooking_success() {
        Booking booking = Booking.builder().id(1L).user(user).venue(venue).timeSlot(slot).activity(activity).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L, user);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertNull(slot.getHeldUntil());
    }

    @Test
    void cancelBooking_alreadyCancelled_throwsException() {
        Booking booking = Booking.builder().id(1L).user(user).status(BookingStatus.CANCELLED).build();
        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.cancelBooking(1L, user));
    }

    @Test
    void getMyBookings_returnsList() {
        Booking b1 = Booking.builder().id(1L).venue(venue).timeSlot(slot).activity(activity).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(b1));

        List<BookingResponse> result = bookingService.getMyBookings(user);

        assertEquals(1, result.size());
        assertEquals(BookingStatus.CONFIRMED, result.get(0).getStatus());
    }
}
