package com.shuttlebooking.booking;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> create(
            @Valid @RequestBody BookingRequest req,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.createBooking(req, user);
        return ApiResponse.ok("Booking created. Complete payment within 10 minutes.", BookingResponse.from(booking));
    }

    @GetMapping("/mine")
    public ApiResponse<List<BookingResponse>> myBookings(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(bookingService.getMyBookings(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(bookingService.getBooking(id, user));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        bookingService.cancelBooking(id, user);
        return ApiResponse.ok("Booking cancelled", null);
    }
}
