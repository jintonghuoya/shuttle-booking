package com.shuttlebooking.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgMemberRepository extends JpaRepository<OrgMember, Long> {

    List<OrgMember> findByOrgId(Long orgId);

    Optional<OrgMember> findByOrgIdAndUserId(Long orgId, Long userId);

    boolean existsByOrgIdAndUserId(Long orgId, Long userId);
}
