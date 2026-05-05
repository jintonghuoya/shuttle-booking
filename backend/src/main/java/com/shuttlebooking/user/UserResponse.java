package com.shuttlebooking.user;

import com.shuttlebooking.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private boolean active;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.isActive());
    }
}
