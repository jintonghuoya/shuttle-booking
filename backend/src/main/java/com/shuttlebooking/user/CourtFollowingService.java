package com.shuttlebooking.user;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.court.Court;
import com.shuttlebooking.court.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtFollowingService {

    private final CourtFollowingRepository courtFollowingRepository;
    private final CourtRepository courtRepository;

    @Transactional
    public boolean toggleFollow(Long courtId, User user) {
        Court court = courtRepository.findById(courtId)
                .filter(Court::isActive)
                .orElseThrow(() -> new BusinessException("Court not found"));

        if (courtFollowingRepository.existsByUserIdAndCourtId(user.getId(), courtId)) {
            courtFollowingRepository.deleteByUserIdAndCourtId(user.getId(), courtId);
            return false;
        }

        CourtFollowing following = CourtFollowing.builder()
                .user(user)
                .court(court)
                .build();
        courtFollowingRepository.save(following);
        return true;
    }

    public boolean isFollowing(Long courtId, User user) {
        return courtFollowingRepository.existsByUserIdAndCourtId(user.getId(), courtId);
    }

    public List<Court> getFollowedCourts(User user) {
        return courtFollowingRepository.findByUserId(user.getId()).stream()
                .map(CourtFollowing::getCourt)
                .filter(Court::isActive)
                .toList();
    }
}
