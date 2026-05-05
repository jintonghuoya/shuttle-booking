package com.shuttlebooking.activity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActivityRequest {

    @NotNull
    private Long orgId;

    @NotNull
    private Long venueId;

    private Long courtId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Min(0)
    @Max(23)
    private Integer startHour;

    @NotNull
    @Min(1)
    @Max(24)
    private Integer endHour;
}
