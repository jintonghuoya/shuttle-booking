package com.shuttlebooking.venue;

import com.shuttlebooking.activity.ActivityResponse;
import com.shuttlebooking.activity.ActivityService;
import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final ActivityService activityService;

    @GetMapping
    public ApiResponse<Page<VenueResponse>> list(Pageable pageable) {
        return ApiResponse.ok(venueService.listActive(pageable));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<VenueResponse>> nearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "10") double radius) {
        return ApiResponse.ok(venueService.findNearby(lat, lng, radius));
    }

    @GetMapping("/{id}")
    public ApiResponse<VenueResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(VenueResponse.from(venueService.getVenueOrThrow(id)));
    }

    @PostMapping
    public ApiResponse<VenueResponse> submit(
            @Valid @RequestBody VenueRequest req,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("Venue submitted, pending approval", venueService.submit(req, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<VenueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest req,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(venueService.update(id, req, user));
    }

    @GetMapping("/my")
    public ApiResponse<List<VenueResponse>> myVenues(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(venueService.findByOrganizer(user.getId()));
    }

    @GetMapping("/{id}/activities")
    public ApiResponse<List<ActivityResponse>> activitiesByVenue(@PathVariable Long id) {
        return ApiResponse.ok(activityService.listByVenue(id).stream()
                .map(ActivityResponse::from).toList());
    }
}
