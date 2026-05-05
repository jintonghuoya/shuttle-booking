package com.shuttlebooking.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String createdByName;
    private Long createdByUserId;
    private boolean active;
    private Instant createdAt;

    public static OrganizationResponse from(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getDescription(),
                org.getLogoUrl(),
                org.getCreatedBy() != null ? org.getCreatedBy().getName() : null,
                org.getCreatedBy() != null ? org.getCreatedBy().getId() : null,
                org.isActive(),
                org.getCreatedAt()
        );
    }
}
