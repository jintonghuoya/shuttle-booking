package com.shuttlebooking.admin;

import com.shuttlebooking.approval.ApprovalRepository;
import com.shuttlebooking.approval.ApprovalRequest;
import com.shuttlebooking.approval.ApprovalResponse;
import com.shuttlebooking.common.ApprovalStatus;
import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserRepository;
import com.shuttlebooking.venue.Venue;
import com.shuttlebooking.venue.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ApprovalRepository approvalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private AdminService adminService;

    private User admin;
    private Venue venue;
    private ApprovalRequest approvalRequest;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).email("admin@test.com").name("Admin").build();
        venue = Venue.builder().id(1L).name("Test Venue").active(false).build();
        User organizer = User.builder().id(2L).name("Org").build();
        approvalRequest = ApprovalRequest.builder().id(1L).venue(venue).submittedBy(organizer).status(ApprovalStatus.PENDING).build();
    }

    @Test
    void approve_setsVenueActive() {
        when(approvalRepository.findById(1L)).thenReturn(Optional.of(approvalRequest));

        ApprovalResponse result = adminService.approve(1L, admin, "Looks good");

        assertEquals(ApprovalStatus.APPROVED, result.getStatus());
        assertTrue(venue.isActive());
        assertEquals(admin, venue.getApprovedBy());
        assertEquals("Looks good", approvalRequest.getReviewNote());
    }

    @Test
    void approve_alreadyProcessed_throwsException() {
        approvalRequest.setStatus(ApprovalStatus.APPROVED);
        when(approvalRepository.findById(1L)).thenReturn(Optional.of(approvalRequest));

        assertThrows(BusinessException.class, () -> adminService.approve(1L, admin, null));
    }

    @Test
    void approve_notFound_throwsException() {
        when(approvalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> adminService.approve(99L, admin, null));
    }

    @Test
    void reject_setsStatusRejected() {
        when(approvalRepository.findById(1L)).thenReturn(Optional.of(approvalRequest));

        ApprovalResponse result = adminService.reject(1L, admin, "Not suitable");

        assertEquals(ApprovalStatus.REJECTED, result.getStatus());
        assertFalse(venue.isActive());
    }

    @Test
    void toggleUser_deactivates() {
        User user = User.builder().id(2L).active(true).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        var result = adminService.toggleUser(2L, admin);

        assertFalse(result.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void toggleUser_activates() {
        User user = User.builder().id(2L).active(false).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        var result = adminService.toggleUser(2L, admin);

        assertTrue(result.isActive());
    }

    @Test
    void listUsers_returnsAll() {
        User u1 = User.builder().id(1L).name("A").build();
        when(userRepository.findAll()).thenReturn(List.of(u1));

        var result = adminService.listUsers();

        assertEquals(1, result.size());
    }
}
