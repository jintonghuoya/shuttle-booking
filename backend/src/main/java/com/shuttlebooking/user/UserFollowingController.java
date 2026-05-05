package com.shuttlebooking.user;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.court.CourtResponse;
import com.shuttlebooking.organization.OrganizationResponse;
import com.shuttlebooking.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFollowingController {

    private final OrganizationService organizationService;
    private final CourtFollowingService courtFollowingService;

    @GetMapping("/following")
    public ApiResponse<List<OrganizationResponse>> following(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(organizationService.getFollowedOrgs(user).stream()
                .map(OrganizationResponse::from).toList());
    }

    @GetMapping("/following/courts")
    public ApiResponse<List<CourtResponse>> followingCourts(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(courtFollowingService.getFollowedCourts(user).stream()
                .map(CourtResponse::from).toList());
    }

    @PostMapping("/following/courts/{courtId}/toggle")
    public ApiResponse<Boolean> toggleCourtFollow(
            @PathVariable Long courtId,
            @AuthenticationPrincipal User user) {
        boolean isFollowing = courtFollowingService.toggleFollow(courtId, user);
        return ApiResponse.ok(isFollowing ? "Now following" : "Unfollowed", isFollowing);
    }

    @GetMapping("/following/courts/{courtId}/check")
    public ApiResponse<Boolean> isFollowingCourt(
            @PathVariable Long courtId,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(courtFollowingService.isFollowing(courtId, user));
    }
}
