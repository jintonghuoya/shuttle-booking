package com.shuttlebooking.auth;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.config.JwtProvider;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserRepository;
import com.shuttlebooking.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ApiResponse.error("Email already registered");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role(req.getRole() == com.shuttlebooking.common.Role.ROLE_ORGANIZER
                        ? com.shuttlebooking.common.Role.ROLE_ORGANIZER
                        : com.shuttlebooking.common.Role.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return ApiResponse.ok(new AuthResponse(token, UserResponse.from(user)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("User not found"));
        String token = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return ApiResponse.ok(new AuthResponse(token, UserResponse.from(user)));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Not authenticated");
        }
        return ApiResponse.ok(UserResponse.from(user));
    }
}
