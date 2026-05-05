package com.shuttlebooking.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull
    private Long courtId;

    @NotNull
    private Long timeSlotId;

    private Long activityId;
}
