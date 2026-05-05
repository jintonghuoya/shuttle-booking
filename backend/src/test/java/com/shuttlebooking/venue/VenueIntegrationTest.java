package com.shuttlebooking.venue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.common.ApprovalStatus;
import com.shuttlebooking.common.Role;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserRepository;
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
class VenueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String organizerToken;

    @BeforeEach
    void setUp() throws Exception {
        User organizer = User.builder()
                .email("org@test.com")
                .passwordHash(passwordEncoder.encode("pass"))
                .name("Org")
                .role(Role.ROLE_ORGANIZER)
                .active(true)
                .build();
        userRepository.save(organizer);

        organizerToken = loginAndGetToken("org@test.com", "pass");
    }

    @Test
    void submitVenue_asOrganizer_createsPendingVenue() throws Exception {
        VenueRequest req = new VenueRequest();
        req.setName("New Court");
        req.setAddress("456 Singapore");
        req.setLatitude(new BigDecimal("1.35"));
        req.setLongitude(new BigDecimal("103.82"));

        mockMvc.perform(post("/venues")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Court"));
    }

    @Test
    void submitVenue_asUser_forbidden() throws Exception {
        User user = User.builder()
                .email("user@test.com")
                .passwordHash(passwordEncoder.encode("pass"))
                .name("User")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);
        String userToken = loginAndGetToken("user@test.com", "pass");

        VenueRequest req = new VenueRequest();
        req.setName("New Court");
        req.setAddress("456 Singapore");
        req.setLatitude(new BigDecimal("1.35"));
        req.setLongitude(new BigDecimal("103.82"));

        mockMvc.perform(post("/venues")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listActive_venuesOnly() throws Exception {
        Venue active = Venue.builder().name("Active").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(true).submittedBy(userRepository.findAll().iterator().next()).build();
        Venue inactive = Venue.builder().name("Inactive").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(false).submittedBy(userRepository.findAll().iterator().next()).build();
        venueRepository.save(active);
        venueRepository.save(inactive);

        mockMvc.perform(get("/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Active"));
    }

    @Test
    void getVenue_active_success() throws Exception {
        User organizer = userRepository.findByEmail("org@test.com").orElseThrow();
        Venue venue = Venue.builder().name("Test").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(true).submittedBy(organizer).build();
        venue = venueRepository.save(venue);

        mockMvc.perform(get("/venues/" + venue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test"));
    }

    @Test
    void getVenue_inactive_notFound() throws Exception {
        User organizer = userRepository.findByEmail("org@test.com").orElseThrow();
        Venue venue = Venue.builder().name("Hidden").address("addr").latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).active(false).submittedBy(organizer).build();
        venue = venueRepository.save(venue);

        mockMvc.perform(get("/venues/" + venue.getId()))
                .andExpect(status().isBadRequest());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        com.shuttlebooking.auth.LoginRequest req = new com.shuttlebooking.auth.LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
