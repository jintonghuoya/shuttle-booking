package com.shuttlebooking.user;

import com.shuttlebooking.organization.OrganizationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserFollowingResponse {
    private Long id;
    private OrganizationResponse org;
    private Instant createdAt;

    public static UserFollowingResponse from(UserFollowing following) {
        return new UserFollowingResponse(
                following.getId(),
                following.getOrg() != null ? OrganizationResponse.from(following.getOrg()) : null,
                following.getCreatedAt()
        );
    }
}
