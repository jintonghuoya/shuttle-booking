package com.shuttlebooking.admin;

import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.approval.ApprovalRequest;
import com.shuttlebooking.approval.ApprovalResponse;
import com.shuttlebooking.common.ApprovalStatus;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserRepository;
import com.shuttlebooking.user.UserResponse;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING).stream()
                .map(ApprovalResponse::from)
                .toList();
    }

    public List<ApprovalResponse> getAllApprovals() {
        return approvalRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ApprovalResponse::from)
                .toList();
    }

    @Transactional
    public ApprovalResponse approve(Long approvalId, User admin, String note) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Request already processed");
        }

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setReviewedBy(admin);
        approval.setReviewNote(note);
        approvalRepository.save(approval);

        Venue venue = approval.getVenue();
        venue.setActive(true);
        venue.setApprovedBy(admin);
        venueRepository.save(venue);

        return ApprovalResponse.from(approval);
    }

    @Transactional
    public ApprovalResponse reject(Long approvalId, User admin, String note) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Request already processed");
        }

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setReviewedBy(admin);
        approval.setReviewNote(note);
        approvalRepository.save(approval);

        return ApprovalResponse.from(approval);
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse toggleUser(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.getId().equals(admin.getId())) {
            throw new BusinessException("Cannot deactivate your own account");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
        return UserResponse.from(user);
    }
}
