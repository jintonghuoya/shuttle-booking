package com.shuttlebooking.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByVenueId(Long venueId);

    List<Activity> findByOrgId(Long orgId);
}
