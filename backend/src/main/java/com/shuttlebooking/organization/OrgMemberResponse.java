package com.shuttlebooking.organization;

import com.shuttlebooking.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class OrgMemberResponse {
    private Long id;
    private UserResponse user;
    private String role;
    private Instant createdAt;

    public static OrgMemberResponse from(OrgMember member) {
        return new OrgMemberResponse(
                member.getId(),
                member.getUser() != null ? UserResponse.from(member.getUser()) : null,
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
