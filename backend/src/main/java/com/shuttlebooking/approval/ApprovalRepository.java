package com.shuttlebooking.approval;

import com.shuttlebooking.common.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    List<ApprovalRequest> findAllByOrderByCreatedAtDesc();
}
