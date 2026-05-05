package com.shuttlebooking.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFollowingRepository extends JpaRepository<UserFollowing, Long> {

    List<UserFollowing> findByUserId(Long userId);

    boolean existsByUserIdAndOrgId(Long userId, Long orgId);

    void deleteByUserIdAndOrgId(Long userId, Long orgId);
}
