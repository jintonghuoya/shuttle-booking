package com.shuttlebooking.booking;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.BookingStatus;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.court.Court;
import com.shuttlebooking.court.CourtRepository;
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
    private CourtRepository courtRepository;
    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Court court;
    private TimeSlot slot;
    private Venue venue;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
        venue = Venue.builder().id(1L).name("Test Venue").active(true).build();
        court = Court.builder().id(1L).venue(venue).courtNumber(1).pricePerHourSgd(new BigDecimal("10.00")).active(true).build();
        slot = TimeSlot.builder().id(1L).court(court).slotDate(LocalDate.now()).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).status(SlotStatus.AVAILABLE).build();
    }

    @Test
    void createBooking_success() {
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingRequest req = new BookingRequest();
        req.setCourtId(1L);
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
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(slot));

        BookingRequest req = new BookingRequest();
        req.setCourtId(1L);
        req.setTimeSlotId(1L);

        assertThrows(BusinessException.class, () -> bookingService.createBooking(req, user));
    }

    @Test
    void createBooking_courtMismatch_throwsException() {
        Court otherCourt = Court.builder().id(99L).venue(venue).build();
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(courtRepository.findById(99L)).thenReturn(Optional.of(otherCourt));

        BookingRequest req = new BookingRequest();
        req.setCourtId(99L);
        req.setTimeSlotId(1L);

        assertThrows(BusinessException.class, () -> bookingService.createBooking(req, user));
    }

    @Test
    void cancelBooking_success() {
        Booking booking = Booking.builder().id(1L).user(user).court(court).venue(venue).timeSlot(slot).status(BookingStatus.CONFIRMED).build();
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
        Booking b1 = Booking.builder().id(1L).venue(venue).court(court).timeSlot(slot).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(b1));

        List<BookingResponse> result = bookingService.getMyBookings(user);

        assertEquals(1, result.size());
        assertEquals(BookingStatus.CONFIRMED, result.get(0).getStatus());
    }
}
