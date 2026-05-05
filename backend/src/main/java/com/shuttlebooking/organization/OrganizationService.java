package com.shuttlebooking.organization;

import com.shuttlebooking.common.BusinessException;
import com.shuttlebooking.user.User;
import com.shuttlebooking.user.UserFollowing;
import com.shuttlebooking.user.UserFollowingRepository;
import com.shuttlebooking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final UserFollowingRepository userFollowingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Organization create(String name, String description, User creator) {
        Organization org = Organization.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .active(true)
                .build();
        org = organizationRepository.save(org);

        OrgMember member = OrgMember.builder()
                .org(org)
                .user(creator)
                .role("OWNER")
                .build();
        orgMemberRepository.save(member);

        return org;
    }

    public List<Organization> listAll() {
        return organizationRepository.findAll().stream()
                .filter(Organization::isActive)
                .toList();
    }

    public Organization getById(Long id) {
        return organizationRepository.findById(id)
                .filter(Organization::isActive)
                .orElseThrow(() -> new BusinessException("Organization not found"));
    }

    public List<Organization> getMyOrgs(User user) {
        return organizationRepository.findAll().stream()
                .filter(Organization::isActive)
                .filter(org -> orgMemberRepository.existsByOrgIdAndUserId(org.getId(), user.getId()))
                .toList();
    }

    @Transactional
    public void addMember(Long orgId, String email, User requester) {
        Organization org = getById(orgId);

        OrgMember requesterMember = orgMemberRepository.findByOrgIdAndUserId(orgId, requester.getId())
                .orElseThrow(() -> new BusinessException("You are not a member of this organization"));

        if (!"OWNER".equals(requesterMember.getRole())) {
            throw new BusinessException("Only owners can add members");
        }

        User newUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email));

        if (orgMemberRepository.existsByOrgIdAndUserId(orgId, newUser.getId())) {
            throw new BusinessException("User is already a member of this organization");
        }

        OrgMember member = OrgMember.builder()
                .org(org)
                .user(newUser)
                .role("MEMBER")
                .build();
        orgMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long orgId, Long userId, User requester) {
        Organization org = getById(orgId);

        OrgMember requesterMember = orgMemberRepository.findByOrgIdAndUserId(orgId, requester.getId())
                .orElseThrow(() -> new BusinessException("You are not a member of this organization"));

        if (!"OWNER".equals(requesterMember.getRole())) {
            throw new BusinessException("Only owners can remove members");
        }

        OrgMember member = orgMemberRepository.findByOrgIdAndUserId(orgId, userId)
                .orElseThrow(() -> new BusinessException("User is not a member of this organization"));

        if ("OWNER".equals(member.getRole())) {
            throw new BusinessException("Cannot remove the owner");
        }

        orgMemberRepository.delete(member);
    }

    public List<OrgMember> getMembers(Long orgId) {
        getById(orgId);
        return orgMemberRepository.findByOrgId(orgId);
    }

    @Transactional
    public boolean toggleFollow(Long orgId, User user) {
        getById(orgId);

        if (userFollowingRepository.existsByUserIdAndOrgId(user.getId(), orgId)) {
            userFollowingRepository.deleteByUserIdAndOrgId(user.getId(), orgId);
            return false;
        } else {
            Organization org = organizationRepository.findById(orgId).orElseThrow();
            UserFollowing following = UserFollowing.builder()
                    .user(user)
                    .org(org)
                    .build();
            userFollowingRepository.save(following);
            return true;
        }
    }

    public boolean isFollowing(Long orgId, User user) {
        return userFollowingRepository.existsByUserIdAndOrgId(user.getId(), orgId);
    }

    public List<Organization> getFollowedOrgs(User user) {
        return userFollowingRepository.findByUserId(user.getId()).stream()
                .map(UserFollowing::getOrg)
                .filter(Organization::isActive)
                .toList();
    }
}
