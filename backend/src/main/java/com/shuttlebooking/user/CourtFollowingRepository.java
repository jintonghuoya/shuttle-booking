package com.shuttlebooking.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtFollowingRepository extends JpaRepository<CourtFollowing, Long> {

    List<CourtFollowing> findByUserId(Long userId);

    boolean existsByUserIdAndCourtId(Long userId, Long courtId);

    void deleteByUserIdAndCourtId(Long userId, Long courtId);
}
