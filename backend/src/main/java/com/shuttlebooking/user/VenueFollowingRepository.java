package com.shuttlebooking.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueFollowingRepository extends JpaRepository<VenueFollowing, Long> {

    List<VenueFollowing> findByUserId(Long userId);

    boolean existsByUserIdAndVenueId(Long userId, Long venueId);

    void deleteByUserIdAndVenueId(Long userId, Long venueId);
}
