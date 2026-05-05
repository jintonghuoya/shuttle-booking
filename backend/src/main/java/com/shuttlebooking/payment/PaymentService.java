package com.shuttlebooking.payment;

import com.shuttlebooking.booking.Booking;
import com.shuttlebooking.common.BookingStatus;
import com.shuttlebooking.booking.BookingRepository;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.PaymentStatus;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public String createCheckoutSession(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException("Booking is not awaiting payment");
        }

        com.stripe.Stripe.apiKey = stripeSecretKey;

        String productName = booking.getVenue().getName();
        if (booking.getActivity() != null && booking.getActivity().getCourtDescription() != null) {
            productName += " - " + booking.getActivity().getCourtDescription();
        }
        productName += " " + booking.getTimeSlot().getSlotDate() + " " + booking.getTimeSlot().getStartTime();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3001/bookings?payment=success&ref=" + booking.getBookingRef())
                .setCancelUrl("http://localhost:3001/venues/" + booking.getVenue().getId() + "?payment=cancelled")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("sgd")
                                .setUnitAmount(booking.getTotalAmount().movePointRight(2).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(productName)
                                        .build())
                                .build())
                        .setQuantity(1L)
                        .build())
                .putMetadata("bookingId", booking.getId().toString())
                .putMetadata("bookingRef", booking.getBookingRef())
                .build();

        try {
            Session session = Session.create(params);

            Payment payment = Payment.builder()
                    .bookingId(bookingId)
                    .stripeSessionId(session.getId())
                    .amount(booking.getTotalAmount())
                    .currency("SGD")
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            return session.getUrl();
        } catch (Exception e) {
            throw new BusinessException("Failed to create payment session: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        com.stripe.Stripe.apiKey = stripeSecretKey;

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            throw new BusinessException("Invalid webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            Long bookingId = Long.parseLong(session.getMetadata().get("bookingId"));

            Payment payment = paymentRepository.findByStripeSessionId(session.getId()).orElse(null);
            if (payment == null) return;

            payment.setStripePaymentIntentId(session.getPaymentIntent());
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null && booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                TimeSlot slot = booking.getTimeSlot();
                slot.setStatus(SlotStatus.BOOKED);
                slot.setHeldUntil(null);
                timeSlotRepository.save(slot);
            }
        }
    }

    @Transactional
    public void processRefund(Booking booking) {
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new BusinessException("Booking must be cancelled before refund");
        }

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new BusinessException("No payment found for this booking"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            return;
        }

        com.stripe.Stripe.apiKey = stripeSecretKey;

        try {
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                    com.stripe.param.RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .build()
            );

            payment.setStripeRefundId(refund.getId());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            booking.setStatus(BookingStatus.REFUNDED);
            bookingRepository.save(booking);
        } catch (Exception e) {
            throw new BusinessException("Refund failed: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentStatus(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BusinessException("No payment found"));
        return PaymentResponse.from(payment);
    }
}
