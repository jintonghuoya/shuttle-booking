package com.shuttlebooking.venue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VenueRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private String description;
    private String phone;
}
