package com.shuttlebooking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuttlebooking.activity.Activity;
import com.shuttlebooking.activity.ActivityRepository;
import com.shuttlebooking.auth.LoginRequest;
import com.shuttlebooking.common.Role;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserRepository;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        User organizer = User.builder().email("org@test.com").passwordHash(passwordEncoder.encode("pass")).name("Org").role(Role.ROLE_ORGANIZER).active(true).build();
        userRepository.save(organizer);

        User user = User.builder().email("user@test.com").passwordHash(passwordEncoder.encode("pass")).name("User").role(Role.ROLE_USER).active(true).build();
        userRepository.save(user);

        userToken = loginAndGetToken("user@test.com", "pass");

        Venue venue = Venue.builder().name("Test Venue").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(true).submittedBy(organizer).build();
        venue = venueRepository.save(venue);

        Activity activity = Activity.builder()
                .org(null).venue(venue).title("Test Activity")
                .startDate(LocalDate.now()).endDate(LocalDate.now())
                .startHour(10).endHour(12).status("PUBLISHED")
                .pricePerHourSgd(new BigDecimal("15.00"))
                .build();
        activity = activityRepository.save(activity);

        TimeSlot slot = TimeSlot.builder().activity(activity).slotDate(LocalDate.now()).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).status(SlotStatus.AVAILABLE).build();
        timeSlotRepository.save(slot);
    }

    @Test
    void createBooking_success() throws Exception {
        TimeSlot slot = timeSlotRepository.findAll().iterator().next();

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(slot.getId());

        mockMvc.perform(post("/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.bookingRef").isNotEmpty());
    }

    @Test
    void createBooking_slotUnavailable_fails() throws Exception {
        TimeSlot slot = timeSlotRepository.findAll().iterator().next();
        slot.setStatus(SlotStatus.BOOKED);
        timeSlotRepository.save(slot);

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(slot.getId());

        mockMvc.perform(post("/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Time slot is not available"));
    }

    @Test
    void myBookings_returnsList() throws Exception {
        mockMvc.perform(get("/bookings/mine")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelBooking_success() throws Exception {
        TimeSlot slot = timeSlotRepository.findAll().iterator().next();

        BookingRequest req = new BookingRequest();
        req.setTimeSlotId(slot.getId());

        String createResponse = mockMvc.perform(post("/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
