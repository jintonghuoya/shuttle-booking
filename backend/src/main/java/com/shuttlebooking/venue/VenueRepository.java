package com.shuttlebooking.venue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findByActiveTrue(Pageable pageable);

    List<Venue> findByActiveTrue();

    @Query("SELECT v FROM Venue v WHERE v.active = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
           "cos(radians(v.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(v.latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
           "cos(radians(v.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(v.latitude))))")
    List<Venue> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm);

    List<Venue> findBySubmittedById(Long userId);
}
