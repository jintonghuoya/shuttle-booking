package com.shuttlebooking.approval;

import com.shuttlebooking.common.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApprovalResponse {
    private Long id;
    private Long venueId;
    private String venueName;
    private String submittedByName;
    private ApprovalStatus status;
    private String reviewNote;
    private String reviewedByName;
    private Instant createdAt;

    public static ApprovalResponse from(ApprovalRequest req) {
        return new ApprovalResponse(
                req.getId(),
                req.getVenue().getId(),
                req.getVenue().getName(),
                req.getSubmittedBy().getName(),
                req.getStatus(),
                req.getReviewNote(),
                req.getReviewedBy() != null ? req.getReviewedBy().getName() : null,
                req.getCreatedAt()
        );
    }
}
