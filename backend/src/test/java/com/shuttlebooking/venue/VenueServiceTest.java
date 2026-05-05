package com.shuttlebooking.venue;

import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.approval.ApprovalRequest;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;
    @Mock
    private ApprovalRepository approvalRepository;

    @InjectMocks
    private VenueService venueService;

    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(1L).email("org@test.com").name("Organizer").build();
    }

    @Test
    void submit_createsVenueAndApproval() {
        VenueRequest req = new VenueRequest();
        req.setName("Test Court");
        req.setAddress("123 Singapore");
        req.setLatitude(new BigDecimal("1.3521"));
        req.setLongitude(new BigDecimal("103.8198"));

        when(venueRepository.save(any())).thenAnswer(inv -> {
            Venue v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VenueResponse result = venueService.submit(req, organizer);

        assertEquals("Test Court", result.getName());
        assertFalse(result.isActive());
        verify(venueRepository).save(any());
        verify(approvalRepository).save(any());
    }

    @Test
    void getVenueOrThrow_notActive_throwsException() {
        Venue venue = Venue.builder().id(1L).active(false).build();
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        assertThrows(BusinessException.class, () -> venueService.getVenueOrThrow(1L));
    }

    @Test
    void getVenueOrThrow_active_returnsVenue() {
        Venue venue = Venue.builder().id(1L).active(true).build();
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        Venue result = venueService.getVenueOrThrow(1L);
        assertTrue(result.isActive());
    }

    @Test
    void update_notOwner_throwsException() {
        User otherUser = User.builder().id(99L).build();
        Venue venue = Venue.builder().id(1L).submittedBy(organizer).build();
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        VenueRequest req = new VenueRequest();
        req.setName("Updated");

        assertThrows(BusinessException.class, () -> venueService.update(1L, req, otherUser));
    }

    @Test
    void update_owner_updatesVenue() {
        Venue venue = Venue.builder().id(1L).submittedBy(organizer).name("Old").build();
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VenueRequest req = new VenueRequest();
        req.setName("New");
        req.setAddress("addr");
        req.setLatitude(BigDecimal.ONE);
        req.setLongitude(BigDecimal.ONE);

        VenueResponse result = venueService.update(1L, req, organizer);

        assertEquals("New", result.getName());
    }

    @Test
    void findNearby_returnsVenuesWithDistance() {
        Venue v1 = Venue.builder().id(1L).name("Near").latitude(new BigDecimal("1.3521")).longitude(new BigDecimal("103.8198")).active(true).submittedBy(organizer).build();
        when(venueRepository.findNearby(any(), any(), anyDouble())).thenReturn(List.of(v1));

        List<VenueResponse> result = venueService.findNearby(new BigDecimal("1.3521"), new BigDecimal("103.8198"), 10);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getDistanceKm());
    }
}
