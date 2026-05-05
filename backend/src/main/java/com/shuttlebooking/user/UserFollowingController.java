package com.shuttlebooking.user;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.organization.OrganizationResponse;
import com.shuttlebooking.organization.OrganizationService;
import com.shuttlebooking.venue.VenueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFollowingController {

    private final OrganizationService organizationService;
    private final VenueFollowingService venueFollowingService;

    @GetMapping("/following")
    public ApiResponse<List<OrganizationResponse>> following(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(organizationService.getFollowedOrgs(user).stream()
                .map(OrganizationResponse::from).toList());
    }

    @GetMapping("/following/venues")
    public ApiResponse<List<VenueResponse>> followingVenues(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(venueFollowingService.getFollowedVenues(user).stream()
                .map(VenueResponse::from).toList());
    }

    @PostMapping("/following/venues/{venueId}/toggle")
    public ApiResponse<Boolean> toggleVenueFollow(
            @PathVariable Long venueId,
            @AuthenticationPrincipal User user) {
        boolean isFollowing = venueFollowingService.toggleFollow(venueId, user);
        return ApiResponse.ok(isFollowing ? "Now following" : "Unfollowed", isFollowing);
    }

    @GetMapping("/following/venues/{venueId}/check")
    public ApiResponse<Boolean> isFollowingVenue(
            @PathVariable Long venueId,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(venueFollowingService.isFollowing(venueId, user));
    }
}
