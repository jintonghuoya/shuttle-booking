package com.shuttlebooking.payment;

import com.shuttlebooking.common.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getBookingId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus());
    }
}
