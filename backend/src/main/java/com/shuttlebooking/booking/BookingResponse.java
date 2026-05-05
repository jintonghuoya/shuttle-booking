package com.shuttlebooking.booking;

import com.shuttlebooking.common.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingRef;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private String venueName;
    private String courtName;
    private int courtNumber;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Instant createdAt;

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(), b.getBookingRef(), b.getStatus(), b.getTotalAmount(),
                b.getVenue().getName(),
                b.getCourt().getName() != null ? b.getCourt().getName() : "Court " + b.getCourt().getCourtNumber(),
                b.getCourt().getCourtNumber(),
                b.getTimeSlot().getSlotDate(), b.getTimeSlot().getStartTime(), b.getTimeSlot().getEndTime(),
                b.getCreatedAt()
        );
    }
}
