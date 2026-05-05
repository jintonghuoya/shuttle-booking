package com.shuttlebooking.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.approval.ApprovalRequest;
import com.shuttlebooking.auth.LoginRequest;
import com.shuttlebooking.common.ApprovalStatus;
import com.shuttlebooking.common.Role;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private ApprovalRepository approvalRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        User admin = User.builder().email("admin@test.com").passwordHash(passwordEncoder.encode("adminpass")).name("Admin").role(Role.ROLE_ADMIN).active(true).build();
        userRepository.save(admin);
        adminToken = loginAndGetToken("admin@test.com", "adminpass");
    }

    @Test
    void getApprovals_asAdmin() throws Exception {
        User organizer = User.builder().email("org@test.com").passwordHash(passwordEncoder.encode("pass")).name("Org").role(Role.ROLE_ORGANIZER).active(true).build();
        userRepository.save(organizer);

        Venue venue = Venue.builder().name("Test").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(false).submittedBy(organizer).build();
        venue = venueRepository.save(venue);

        ApprovalRequest approval = ApprovalRequest.builder().venue(venue).submittedBy(organizer).status(ApprovalStatus.PENDING).build();
        approvalRepository.save(approval);

        mockMvc.perform(get("/admin/approvals")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].venueName").value("Test"));
    }

    @Test
    void approveVenue_success() throws Exception {
        User organizer = User.builder().email("org@test.com").passwordHash(passwordEncoder.encode("pass")).name("Org").role(Role.ROLE_ORGANIZER).active(true).build();
        userRepository.save(organizer);

        Venue venue = Venue.builder().name("Test").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(false).submittedBy(organizer).build();
        venue = venueRepository.save(venue);

        ApprovalRequest approval = ApprovalRequest.builder().venue(venue).submittedBy(organizer).status(ApprovalStatus.PENDING).build();
        approval = approvalRepository.save(approval);

        mockMvc.perform(post("/admin/approvals/" + approval.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void listUsers_asAdmin() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("admin@test.com"));
    }

    @Test
    void toggleUser_success() throws Exception {
        User user = User.builder().email("user@test.com").passwordHash(passwordEncoder.encode("pass")).name("User").role(Role.ROLE_USER).active(true).build();
        user = userRepository.save(user);

        mockMvc.perform(put("/admin/users/" + user.getId() + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void getApprovals_asUser_forbidden() throws Exception {
        User user = User.builder().email("user@test.com").passwordHash(passwordEncoder.encode("pass")).name("User").role(Role.ROLE_USER).active(true).build();
        userRepository.save(user);
        String userToken = loginAndGetToken("user@test.com", "pass");

        mockMvc.perform(get("/admin/approvals")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
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
