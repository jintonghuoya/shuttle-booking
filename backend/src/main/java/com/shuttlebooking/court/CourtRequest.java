package com.shuttlebooking.court;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourtRequest {
    @NotNull
    @Positive
    private int courtNumber;

    private String name;

    @NotNull
    @Positive
    private BigDecimal pricePerHourSgd;
}
