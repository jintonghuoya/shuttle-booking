package com.shuttlebooking.activity;

import com.shuttlebooking.organization.OrganizationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ActivityResponse {
    private Long id;
    private OrganizationResponse org;
    private Long venueId;
    private String venueName;
    private String courtDescription;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer startHour;
    private Integer endHour;
    private String status;
    private BigDecimal pricePerHourSgd;
    private Instant createdAt;

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getOrg() != null ? OrganizationResponse.from(activity.getOrg()) : null,
                activity.getVenue() != null ? activity.getVenue().getId() : null,
                activity.getVenue() != null ? activity.getVenue().getName() : null,
                activity.getCourtDescription(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getStartDate(),
                activity.getEndDate(),
                activity.getStartHour(),
                activity.getEndHour(),
                activity.getStatus(),
                activity.getPricePerHourSgd(),
                activity.getCreatedAt()
        );
    }
}
