package com.shuttlebooking.auth;

import com.shuttlebooking.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
}
