package com.shuttlebooking.user;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueFollowingService {

    private final VenueFollowingRepository venueFollowingRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public boolean toggleFollow(Long venueId, User user) {
        Venue venue = venueRepository.findById(venueId)
                .filter(Venue::isActive)
                .orElseThrow(() -> new BusinessException("Venue not found"));

        if (venueFollowingRepository.existsByUserIdAndVenueId(user.getId(), venueId)) {
            venueFollowingRepository.deleteByUserIdAndVenueId(user.getId(), venueId);
            return false;
        }

        VenueFollowing following = VenueFollowing.builder()
                .user(user)
                .venue(venue)
                .build();
        venueFollowingRepository.save(following);
        return true;
    }

    public boolean isFollowing(Long venueId, User user) {
        return venueFollowingRepository.existsByUserIdAndVenueId(user.getId(), venueId);
    }

    public List<Venue> getFollowedVenues(User user) {
        return venueFollowingRepository.findByUserId(user.getId()).stream()
                .map(VenueFollowing::getVenue)
                .filter(Venue::isActive)
                .toList();
    }
}
