package com.shuttlebooking.payment;

import com.shuttlebooking.booking.Booking;
import com.shuttlebooking.booking.BookingRepository;
import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;

    @PostMapping("/checkout")
    public ApiResponse<String> createCheckout(@RequestParam Long bookingId) {
        verifyBookingOwnership(bookingId);
        String url = paymentService.createCheckoutSession(bookingId);
        return ApiResponse.ok(url);
    }

    @PostMapping("/webhook")
    public ApiResponse<Void> webhook(@RequestBody String payload,
                                     @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ApiResponse.ok("Webhook processed", null);
    }

    @GetMapping("/{bookingId}/status")
    public ApiResponse<PaymentResponse> status(@PathVariable Long bookingId) {
        verifyBookingOwnership(bookingId);
        return ApiResponse.ok(paymentService.getPaymentStatus(bookingId));
    }

    private void verifyBookingOwnership(Long bookingId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new BusinessException("Not authenticated");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Access denied");
        }
    }
}
