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
    private String courtDescription;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Instant createdAt;

    public static BookingResponse from(Booking b) {
        String courtDesc = null;
        if (b.getActivity() != null) {
            courtDesc = b.getActivity().getCourtDescription();
        }
        return new BookingResponse(
                b.getId(), b.getBookingRef(), b.getStatus(), b.getTotalAmount(),
                b.getVenue().getName(),
                courtDesc,
                b.getTimeSlot().getSlotDate(), b.getTimeSlot().getStartTime(), b.getTimeSlot().getEndTime(),
                b.getCreatedAt()
        );
    }
}
