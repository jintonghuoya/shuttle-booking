package com.shuttlebooking.timeslot;

import com.shuttlebooking.common.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;

    public static TimeSlotResponse from(TimeSlot slot) {
        return new TimeSlotResponse(slot.getId(), slot.getSlotDate(), slot.getStartTime(),
                slot.getEndTime(), slot.getStatus());
    }
}
