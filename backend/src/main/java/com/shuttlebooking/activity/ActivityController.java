package com.shuttlebooking.activity;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ApiResponse<Activity> create(
            @Valid @RequestBody ActivityRequest req,
            @AuthenticationPrincipal User user) {
        Activity activity = activityService.create(req.getOrgId(), req, user);
        return ApiResponse.ok("Activity created", activity);
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> detail(@PathVariable Long id) {
        return ApiResponse.ok(activityService.getById(id));
    }

    @GetMapping("/{id}/slots")
    public ApiResponse<List<TimeSlot>> slots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(activityService.getSlots(id, date));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        activityService.cancel(id, user);
        return ApiResponse.ok("Activity cancelled", null);
    }
}
