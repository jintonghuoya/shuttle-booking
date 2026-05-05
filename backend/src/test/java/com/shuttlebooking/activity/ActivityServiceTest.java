package com.shuttlebooking.activity;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.common.SlotStatus;
import java.util.stream.StreamSupport;
import com.shuttlebooking.court.Court;
import com.shuttlebooking.court.CourtRepository;
import com.shuttlebooking.organization.OrgMemberRepository;
import com.shuttlebooking.organization.Organization;
import com.shuttlebooking.organization.OrganizationRepository;
import com.shuttlebooking.timeslot.TimeSlot;
import com.shuttlebooking.timeslot.TimeSlotRepository;
import com.shuttlebooking.user.User;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrgMemberRepository orgMemberRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private ActivityService activityService;

    private User user;
    private Organization org;
    private Venue venue;
    private Court court;
    private ActivityRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
        org = Organization.builder().id(1L).name("Test Org").active(true).build();
        venue = Venue.builder().id(1L).name("Test Venue").active(true).build();
        court = Court.builder().id(1L).venue(venue).courtNumber(1).active(true).build();

        request = new ActivityRequest();
        request.setOrgId(1L);
        request.setVenueId(1L);
        request.setCourtId(1L);
        request.setTitle("Test Activity");
        request.setDescription("Description");
        request.setStartDate(LocalDate.of(2026, 5, 1));
        request.setEndDate(LocalDate.of(2026, 5, 1));
        request.setStartHour(10);
        request.setEndHour(12);
    }

    @Test
    void create_success() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(activityRepository.save(any())).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        Activity result = activityService.create(1L, request, user);

        assertNotNull(result);
        assertEquals("Test Activity", result.getTitle());
        assertEquals("PUBLISHED", result.getStatus());
        assertEquals(org, result.getOrg());
        assertEquals(venue, result.getVenue());
        assertEquals(court, result.getCourt());
        // 1 day * 2 hours = 2 slots
        verify(timeSlotRepository).saveAll(argThat(slots ->
                StreamSupport.stream(slots.spliterator(), false).count() == 2));
    }

    @Test
    void create_orgNotFound_throwsException() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> activityService.create(99L, request, user));
    }

    @Test
    void create_notMember_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_venueNotFound_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());
        request.setVenueId(99L);

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_courtNotFound_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.findById(99L)).thenReturn(Optional.empty());
        request.setCourtId(99L);

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_courtNotInVenue_throwsException() {
        Venue otherVenue = Venue.builder().id(2L).active(true).build();
        Court otherCourt = Court.builder().id(2L).venue(otherVenue).active(true).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.findById(2L)).thenReturn(Optional.of(otherCourt));
        request.setCourtId(2L);

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_startDateAfterEndDate_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        request.setStartDate(LocalDate.of(2026, 5, 10));
        request.setEndDate(LocalDate.of(2026, 5, 1));

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_startHourNotBeforeEndHour_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        request.setStartHour(12);
        request.setEndHour(10);

        assertThrows(BusinessException.class, () -> activityService.create(1L, request, user));
    }

    @Test
    void create_withoutCourt_success() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        request.setCourtId(null);
        when(activityRepository.save(any())).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        Activity result = activityService.create(1L, request, user);

        assertNotNull(result);
        assertNull(result.getCourt());
        assertEquals("Test Activity", result.getTitle());
    }

    @Test
    void getById_found() {
        Activity activity = Activity.builder().id(1L).title("Test").org(org).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        Activity result = activityService.getById(1L);

        assertEquals("Test", result.getTitle());
    }

    @Test
    void getById_notFound_throwsException() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> activityService.getById(99L));
    }

    @Test
    void listByOrg_filtersPublishedOnly() {
        Activity published = Activity.builder().id(1L).status("PUBLISHED").org(org).build();
        Activity cancelled = Activity.builder().id(2L).status("CANCELLED").org(org).build();
        when(activityRepository.findByOrgId(1L)).thenReturn(List.of(published, cancelled));

        List<Activity> result = activityService.listByOrg(1L);

        assertEquals(1, result.size());
        assertEquals("PUBLISHED", result.get(0).getStatus());
    }

    @Test
    void listByVenue_filtersPublishedOnly() {
        Activity published = Activity.builder().id(1L).status("PUBLISHED").venue(venue).build();
        Activity cancelled = Activity.builder().id(2L).status("CANCELLED").venue(venue).build();
        when(activityRepository.findByVenueId(1L)).thenReturn(List.of(published, cancelled));

        List<Activity> result = activityService.listByVenue(1L);

        assertEquals(1, result.size());
        assertEquals("PUBLISHED", result.get(0).getStatus());
    }

    @Test
    void cancel_success() {
        Activity activity = Activity.builder().id(1L).status("PUBLISHED").org(org).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        TimeSlot slot = TimeSlot.builder().id(1L).status(SlotStatus.AVAILABLE).activity(activity).build();
        when(timeSlotRepository.findByActivityId(1L)).thenReturn(List.of(slot));

        activityService.cancel(1L, user);

        assertEquals("CANCELLED", activity.getStatus());
        verify(activityRepository).save(activity);
        assertNull(slot.getActivity());
        verify(timeSlotRepository).save(slot);
    }

    @Test
    void cancel_notMember_throwsException() {
        Activity activity = Activity.builder().id(1L).status("PUBLISHED").org(org).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> activityService.cancel(1L, user));
    }
}
