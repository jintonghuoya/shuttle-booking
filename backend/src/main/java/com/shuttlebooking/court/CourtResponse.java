package com.shuttlebooking.court;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CourtResponse {
    private Long id;
    private int courtNumber;
    private String name;
    private BigDecimal pricePerHourSgd;
    private boolean active;
    private Long venueId;
    private String venueName;

    public static CourtResponse from(Court court) {
        return new CourtResponse(court.getId(), court.getCourtNumber(), court.getName(),
                court.getPricePerHourSgd(), court.isActive(),
                court.getVenue() != null ? court.getVenue().getId() : null,
                court.getVenue() != null ? court.getVenue().getName() : null);
    }
}
