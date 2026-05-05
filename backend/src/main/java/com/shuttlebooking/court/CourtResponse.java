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

    public static CourtResponse from(Court court) {
        return new CourtResponse(court.getId(), court.getCourtNumber(), court.getName(),
                court.getPricePerHourSgd(), court.isActive());
    }
}
