package com.shuttlebooking.organization;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserFollowing;
import com.shuttlebooking.user.UserFollowingRepository;
import com.shuttlebooking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrgMemberRepository orgMemberRepository;
    @Mock
    private UserFollowingRepository userFollowingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private User owner;
    private User member;
    private Organization org;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@test.com").name("Owner").build();
        member = User.builder().id(2L).email("member@test.com").name("Member").build();
        org = Organization.builder().id(1L).name("Test Org").description("Desc").createdBy(owner).active(true).build();
    }

    @Test
    void create_org_success() {
        when(organizationRepository.save(any())).thenAnswer(inv -> {
            Organization o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Organization result = organizationService.create("Test Org", "Desc", owner);

        assertNotNull(result);
        assertEquals("Test Org", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals(owner, result.getCreatedBy());
        assertTrue(result.isActive());
        verify(organizationRepository).save(any(Organization.class));
        verify(orgMemberRepository).save(argThat(m ->
                "OWNER".equals(m.getRole()) && m.getUser().equals(owner)
        ));
    }

    @Test
    void listAll_onlyActive() {
        Organization inactive = Organization.builder().id(2L).name("Inactive").active(false).build();
        when(organizationRepository.findAll()).thenReturn(List.of(org, inactive));

        List<Organization> result = organizationService.listAll();

        assertEquals(1, result.size());
        assertEquals("Test Org", result.get(0).getName());
    }

    @Test
    void getById_found() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

        Organization result = organizationService.getById(1L);

        assertEquals(org, result);
    }

    @Test
    void getById_notFound_throwsException() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> organizationService.getById(99L));
    }

    @Test
    void getMyOrgs_returnsOrgsUserIsMemberOf() {
        Organization org2 = Organization.builder().id(2L).name("Org 2").active(true).build();
        when(organizationRepository.findAll()).thenReturn(List.of(org, org2));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 1L)).thenReturn(true);
        when(orgMemberRepository.existsByOrgIdAndUserId(2L, 1L)).thenReturn(false);

        List<Organization> result = organizationService.getMyOrgs(owner);

        assertEquals(1, result.size());
        assertEquals("Test Org", result.get(0).getName());
    }

    @Test
    void addMember_success() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("OWNER").build()));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.of(member));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 2L)).thenReturn(false);

        organizationService.addMember(1L, "new@test.com", owner);

        verify(orgMemberRepository).save(argThat(m ->
                "MEMBER".equals(m.getRole()) && m.getUser().equals(member)
        ));
    }

    @Test
    void addMember_notOwner_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("MEMBER").build()));

        assertThrows(BusinessException.class, () -> organizationService.addMember(1L, "new@test.com", owner));
    }

    @Test
    void addMember_userNotFound_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("OWNER").build()));
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> organizationService.addMember(1L, "nobody@test.com", owner));
    }

    @Test
    void addMember_alreadyMember_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("OWNER").build()));
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(member));
        when(orgMemberRepository.existsByOrgIdAndUserId(1L, 2L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> organizationService.addMember(1L, "existing@test.com", owner));
    }

    @Test
    void removeMember_success() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("OWNER").build()));
        OrgMember targetMember = OrgMember.builder().id(10L).org(org).user(member).role("MEMBER").build();
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 2L)).thenReturn(Optional.of(targetMember));

        organizationService.removeMember(1L, 2L, owner);

        verify(orgMemberRepository).delete(targetMember);
    }

    @Test
    void removeMember_cannotRemoveOwner_throwsException() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(
                Optional.of(OrgMember.builder().org(org).user(owner).role("OWNER").build()));
        OrgMember ownerMember = OrgMember.builder().id(10L).org(org).user(owner).role("OWNER").build();
        when(orgMemberRepository.findByOrgIdAndUserId(1L, 1L)).thenReturn(Optional.of(ownerMember));

        assertThrows(BusinessException.class, () -> organizationService.removeMember(1L, 1L, owner));
    }

    @Test
    void toggleFollow_followsAndUnfollows() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

        // First call: follow (not currently following)
        when(userFollowingRepository.existsByUserIdAndOrgId(2L, 1L)).thenReturn(false);
        boolean followed = organizationService.toggleFollow(1L, member);
        assertTrue(followed);
        verify(userFollowingRepository).save(any(UserFollowing.class));

        // Second call: unfollow (currently following)
        when(userFollowingRepository.existsByUserIdAndOrgId(2L, 1L)).thenReturn(true);
        boolean unfollowed = organizationService.toggleFollow(1L, member);
        assertFalse(unfollowed);
        verify(userFollowingRepository).deleteByUserIdAndOrgId(2L, 1L);
    }
}
