package com.shuttlebooking.organization;

import com.shuttlebooking.activity.ActivityResponse;
import com.shuttlebooking.activity.ActivityService;
import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final ActivityService activityService;

    @PostMapping
    public ApiResponse<OrganizationResponse> create(
            @Valid @RequestBody CreateOrgRequest req,
            @AuthenticationPrincipal User user) {
        Organization org = organizationService.create(req.getName(), req.getDescription(), user);
        return ApiResponse.ok("Organization created", OrganizationResponse.from(org));
    }

    @GetMapping
    public ApiResponse<List<OrganizationResponse>> listAll() {
        return ApiResponse.ok(organizationService.listAll().stream()
                .map(OrganizationResponse::from).toList());
    }

    @GetMapping("/mine")
    public ApiResponse<List<OrganizationResponse>> myOrgs(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(organizationService.getMyOrgs(user).stream()
                .map(OrganizationResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(OrganizationResponse.from(organizationService.getById(id)));
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<OrgMemberResponse>> members(@PathVariable Long id) {
        return ApiResponse.ok(organizationService.getMembers(id).stream()
                .map(OrgMemberResponse::from).toList());
    }

    @PostMapping("/{id}/members")
    public ApiResponse<Void> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest req,
            @AuthenticationPrincipal User user) {
        organizationService.addMember(id, req.getEmail(), user);
        return ApiResponse.ok("Member added", null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ApiResponse<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal User user) {
        organizationService.removeMember(id, userId, user);
        return ApiResponse.ok("Member removed", null);
    }

    @PostMapping("/{id}/follow")
    public ApiResponse<Boolean> toggleFollow(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        boolean isFollowing = organizationService.toggleFollow(id, user);
        return ApiResponse.ok(isFollowing ? "Now following" : "Unfollowed", isFollowing);
    }

    @GetMapping("/{id}/following")
    public ApiResponse<Boolean> isFollowing(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(organizationService.isFollowing(id, user));
    }

    @GetMapping("/{id}/activities")
    public ApiResponse<List<ActivityResponse>> activitiesByOrg(@PathVariable Long id) {
        return ApiResponse.ok(activityService.listByOrg(id).stream()
                .map(ActivityResponse::from).toList());
    }

    @Data
    static class CreateOrgRequest {
        @NotBlank
        private String name;
        private String description;
    }

    @Data
    static class AddMemberRequest {
        @NotBlank
        private String email;
    }
}
