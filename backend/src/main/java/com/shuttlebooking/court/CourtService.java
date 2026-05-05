package com.shuttlebooking.court;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.timeslot.TimeSlotResponse;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;

    public List<CourtResponse> listByVenue(Long venueId) {
        return courtRepository.findByVenueIdAndActiveTrue(venueId).stream()
                .map(CourtResponse::from)
                .toList();
    }

    public List<TimeSlotResponse> getSlots(Long courtId, LocalDate date) {
        return timeSlotRepository.findByCourtIdAndSlotDate(courtId, date).stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    @Transactional
    public CourtResponse addCourt(Long venueId, CourtRequest req) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException("Venue not found"));

        Court court = Court.builder()
                .venue(venue)
                .courtNumber(req.getCourtNumber())
                .name(req.getName())
                .pricePerHourSgd(req.getPricePerHourSgd())
                .active(true)
                .build();
        court = courtRepository.save(court);

        generateSlotsForNext7Days(court);

        return CourtResponse.from(court);
    }

    private void generateSlotsForNext7Days(Court court) {
        LocalDate today = LocalDate.now();
        for (int d = 0; d < 7; d++) {
            LocalDate date = today.plusDays(d);
            for (int hour = 6; hour < 23; hour++) {
                TimeSlot slot = TimeSlot.builder()
                        .court(court)
                        .slotDate(date)
                        .startTime(LocalTime.of(hour, 0))
                        .endTime(LocalTime.of(hour + 1, 0))
                        .build();
                try {
                    timeSlotRepository.save(slot);
                } catch (Exception e) {
                    // slot already exists, skip
                }
            }
        }
    }
}
