package com.shuttlebooking.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("password123");
        req.setName("New User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("new@test.com"));
    }

    @Test
    void register_duplicateEmail_fails() throws Exception {
        User existing = User.builder()
                .email("dup@test.com")
                .passwordHash(passwordEncoder.encode("pass"))
                .name("Existing")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(existing);

        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@test.com");
        req.setPassword("password123");
        req.setName("Duplicate");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void login_success() throws Exception {
        User user = User.builder()
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("Login User")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);

        LoginRequest req = new LoginRequest();
        req.setEmail("login@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_fails() throws Exception {
        User user = User.builder()
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("Login User")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);

        LoginRequest req = new LoginRequest();
        req.setEmail("login@test.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_authenticated_returnsUser() throws Exception {
        User user = User.builder()
                .email("me@test.com")
                .passwordHash(passwordEncoder.encode("pass"))
                .name("Me User")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);

        String token = loginAndGetToken("me@test.com", "pass");

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@test.com"));
    }

    @Test
    void me_noToken_unauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
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
