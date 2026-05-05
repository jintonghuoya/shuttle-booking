package com.shuttlebooking.venue;

import com.shuttlebooking.approval.ApprovalRequest;
import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final ApprovalRepository approvalRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public Page<VenueResponse> listActive(Pageable pageable) {
        return venueRepository.findByActiveTrue(pageable).map(VenueResponse::from);
    }

    public List<VenueResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
        List<Venue> venues = venueRepository.findNearby(lat, lng, radiusKm);
        return venues.stream()
                .map(v -> VenueResponse.from(v, calculateDistance(lat, lng, v.getLatitude(), v.getLongitude())))
                .toList();
    }

    public Venue getVenueOrThrow(Long id) {
        return venueRepository.findById(id)
                .filter(Venue::isActive)
                .orElseThrow(() -> new BusinessException("Venue not found"));
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Venue not found"));
    }

    public List<VenueResponse> findByOrganizer(Long userId) {
        return venueRepository.findBySubmittedById(userId).stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional
    public VenueResponse submit(VenueRequest req, User organizer) {
        Venue venue = Venue.builder()
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .description(req.getDescription())
                .phone(req.getPhone())
                .active(false)
                .submittedBy(organizer)
                .build();
        venue = venueRepository.save(venue);

        ApprovalRequest approval = ApprovalRequest.builder()
                .venue(venue)
                .submittedBy(organizer)
                .build();
        approvalRepository.save(approval);

        return VenueResponse.from(venue);
    }

    @Transactional
    public VenueResponse createDirectly(VenueRequest req, User admin) {
        Venue venue = Venue.builder()
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .description(req.getDescription())
                .phone(req.getPhone())
                .active(true)
                .submittedBy(admin)
                .approvedBy(admin)
                .build();
        venue = venueRepository.save(venue);
        return VenueResponse.from(venue);
    }

    @Transactional
    public VenueResponse update(Long id, VenueRequest req, User organizer) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Venue not found"));

        if (!venue.getSubmittedBy().getId().equals(organizer.getId())) {
            throw new BusinessException("You can only update your own venues");
        }

        venue.setName(req.getName());
        venue.setAddress(req.getAddress());
        venue.setLatitude(req.getLatitude());
        venue.setLongitude(req.getLongitude());
        venue.setDescription(req.getDescription());
        venue.setPhone(req.getPhone());
        venue = venueRepository.save(venue);

        if (!venue.isActive()) {
            ApprovalRequest approval = ApprovalRequest.builder()
                    .venue(venue)
                    .submittedBy(organizer)
                    .build();
            approvalRepository.save(approval);
        }

        return VenueResponse.from(venue);
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
