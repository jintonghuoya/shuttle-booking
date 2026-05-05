package com.shuttlebooking.activity;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.SlotStatus;
import com.shuttlebooking.organization.OrgMemberRepository;
import com.shuttlebooking.organization.Organization;
import com.shuttlebooking.organization.OrganizationRepository;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.user.User;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional
    public Activity create(Long orgId, ActivityRequest req, User user) {
        Organization org = organizationRepository.findById(orgId)
                .filter(Organization::isActive)
                .orElseThrow(() -> new BusinessException("Organization not found"));

        if (!orgMemberRepository.existsByOrgIdAndUserId(orgId, user.getId())) {
            throw new BusinessException("You are not a member of this organization");
        }

        Venue venue = venueRepository.findById(req.getVenueId())
                .filter(Venue::isActive)
                .orElseThrow(() -> new BusinessException("Venue not found"));

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BusinessException("Start date must be before end date");
        }

        if (req.getStartHour() >= req.getEndHour()) {
            throw new BusinessException("Start hour must be before end hour");
        }

        Activity activity = Activity.builder()
                .org(org)
                .venue(venue)
                .courtDescription(req.getCourtDescription())
                .title(req.getTitle())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startHour(req.getStartHour())
                .endHour(req.getEndHour())
                .status("PUBLISHED")
                .pricePerHourSgd(req.getPricePerHourSgd())
                .build();
        activity = activityRepository.save(activity);

        // Auto-generate time slots
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate currentDate = req.getStartDate();
        while (!currentDate.isAfter(req.getEndDate())) {
            for (int hour = req.getStartHour(); hour < req.getEndHour(); hour++) {
                TimeSlot slot = TimeSlot.builder()
                        .slotDate(currentDate)
                        .startTime(LocalTime.of(hour, 0))
                        .endTime(LocalTime.of(hour + 1, 0))
                        .status(SlotStatus.AVAILABLE)
                        .activity(activity)
                        .build();
                slots.add(slot);
            }
            currentDate = currentDate.plusDays(1);
        }
        timeSlotRepository.saveAll(slots);

        return activity;
    }

    public Activity getById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Activity not found"));
    }

    public List<Activity> listByOrg(Long orgId) {
        return activityRepository.findByOrgId(orgId).stream()
                .filter(a -> "PUBLISHED".equals(a.getStatus()))
                .toList();
    }

    public List<Activity> listByVenue(Long venueId) {
        return activityRepository.findByVenueId(venueId).stream()
                .filter(a -> "PUBLISHED".equals(a.getStatus()))
                .toList();
    }

    public List<TimeSlot> getSlots(Long activityId, LocalDate date) {
        Activity activity = getById(activityId);
        return timeSlotRepository.findByActivityIdAndSlotDate(activityId, date);
    }

    @Transactional
    public void cancel(Long id, User user) {
        Activity activity = getById(id);

        if (!orgMemberRepository.existsByOrgIdAndUserId(activity.getOrg().getId(), user.getId())) {
            throw new BusinessException("You are not a member of this organization");
        }

        activity.setStatus("CANCELLED");
        activityRepository.save(activity);

        // Cancel all associated time slots
        List<TimeSlot> slots = timeSlotRepository.findByActivityId(id);
        for (TimeSlot slot : slots) {
            if (slot.getStatus() == SlotStatus.AVAILABLE) {
                slot.setActivity(null);
                timeSlotRepository.save(slot);
            }
        }
    }
}
