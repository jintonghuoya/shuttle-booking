package com.shuttlebooking.court;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.timeslot.TimeSlotResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/venues/{venueId}/courts")
    public ApiResponse<List<CourtResponse>> listByVenue(@PathVariable Long venueId) {
        return ApiResponse.ok(courtService.listByVenue(venueId));
    }

    @GetMapping("/courts/{courtId}/slots")
    public ApiResponse<List<TimeSlotResponse>> getSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(courtService.getSlots(courtId, date));
    }

    @PostMapping("/venues/{venueId}/courts")
    public ApiResponse<CourtResponse> addCourt(
            @PathVariable Long venueId,
            @Valid @RequestBody CourtRequest req) {
        return ApiResponse.ok(courtService.addCourt(venueId, req));
    }
}
