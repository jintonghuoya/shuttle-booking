package com.shuttlebooking.venue;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String phone;
    private boolean active;
    private String submittedByName;
    private Double distanceKm;

    public static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getAddress(),
                venue.getLatitude(), venue.getLongitude(), venue.getDescription(),
                venue.getPhone(), venue.isActive(),
                venue.getSubmittedBy() != null ? venue.getSubmittedBy().getName() : null,
                null);
    }

    public static VenueResponse from(Venue venue, double distanceKm) {
        VenueResponse resp = from(venue);
        resp.setDistanceKm(distanceKm);
        return resp;
    }
}
