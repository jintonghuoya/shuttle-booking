package com.shuttlebooking.admin;

import com.shuttlebooking.approval.ApprovalResponse;
import com.shuttlebooking.common.ApiResponse;
import com.shuttlebooking.organization.Organization;
import com.shuttlebooking.organization.OrganizationService;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrganizationService organizationService;

    @GetMapping("/approvals")
    public ApiResponse<List<ApprovalResponse>> pendingApprovals() {
        return ApiResponse.ok(adminService.getPendingApprovals());
    }

    @GetMapping("/approvals/all")
    public ApiResponse<List<ApprovalResponse>> allApprovals() {
        return ApiResponse.ok(adminService.getAllApprovals());
    }

    @PostMapping("/approvals/{id}/approve")
    public ApiResponse<ApprovalResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) ReviewRequest req) {
        String note = req != null ? req.getNote() : null;
        return ApiResponse.ok("Venue approved", adminService.approve(id, user, note));
    }

    @PostMapping("/approvals/{id}/reject")
    public ApiResponse<ApprovalResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) ReviewRequest req) {
        String note = req != null ? req.getNote() : null;
        return ApiResponse.ok("Venue rejected", adminService.reject(id, user, note));
    }

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.ok(adminService.listUsers());
    }

    @PutMapping("/users/{id}/toggle")
    public ApiResponse<UserResponse> toggleUser(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ApiResponse.ok(adminService.toggleUser(id, user));
    }

    @GetMapping("/org-approvals")
    public ApiResponse<List<Organization>> orgApprovals() {
        return ApiResponse.ok(organizationService.listAll());
    }

    @Data
    static class ReviewRequest {
        private String note;
    }
}
