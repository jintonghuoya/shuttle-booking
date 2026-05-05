package com.shuttlebooking.user;

import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.organization.Organization;
import com.shuttlebooking.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFollowingController {

    private final OrganizationService organizationService;

    @GetMapping("/following")
    public ApiResponse<List<Organization>> following(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(organizationService.getFollowedOrgs(user));
    }
}
