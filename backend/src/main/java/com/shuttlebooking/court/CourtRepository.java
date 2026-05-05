package com.shuttlebooking.court;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByVenueIdAndActiveTrue(Long venueId);
    List<Court> findByVenueId(Long venueId);
}
