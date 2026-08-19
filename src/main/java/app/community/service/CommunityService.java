package app.community.service;

import app.common.exception.ForbiddenOperationException;
import app.common.exception.ResourceConflictException;
import app.common.exception.ResourceNotFoundException;
import app.community.dto.UpsertCommunityRequest;
import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.community.model.CommunityRole;
import app.community.repository.CommunityMembershipRepository;
import app.community.repository.CommunityRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final UserService userService;

    @Transactional
    public Community createCommunity(UUID ownerId, UpsertCommunityRequest request) {

        if (communityRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Community with name [%s] already exists.".formatted(request.getName()));
        }

        User owner = userService.getById(ownerId);

        Community community = communityRepository.save(
                initializeCommunity(request.getName(), request.getDescription(), owner));

        CommunityMembership membership = initializeCommunityMembership(owner, community, CommunityRole.MODERATOR);
        membershipRepository.save(membership);

        return community;
    }

    @Transactional
    public Community updateCommunityInfo(UUID communityId, UUID userId, UpsertCommunityRequest request) {
        Community community = getById(communityId);

        if (!community.getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException("Only the community owner can update its info.");
        }

        String newName = request.getName();
        if (!community.getName().equals(newName) && communityRepository.existsByName(newName)) {
            throw new ResourceConflictException("Community with name [%s] already exists.".formatted(newName));
        }

        community.setName(newName);
        community.setDescription(request.getDescription());
        community.setUpdatedOn(LocalDateTime.now());
        return communityRepository.save(community);
    }

    public CommunityMembership joinCommunity(UUID userId, UUID communityId) {
        Community community = getById(communityId);
        User member = userService.getById(userId);

        if (membershipRepository.existsByMemberAndCommunity(member, community)) {
            throw new ResourceConflictException("User [%s] is already a member of community [%s]."
                    .formatted(member.getUsername(), community.getName()));
        }

        return membershipRepository.save(initializeCommunityMembership(member, community, CommunityRole.MEMBER));
    }

    private Community initializeCommunity(String name, String description, User owner) {
        LocalDateTime now = LocalDateTime.now();

        return Community.builder()
                .name(name)
                .description(description)
                .owner(owner)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    private CommunityMembership initializeCommunityMembership(User member, Community community, CommunityRole role) {
        return CommunityMembership.builder()
                .member(member)
                .community(community)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public Community getById(UUID id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community with id [%s] does not exist.".formatted(id)));
    }

    public Page<Community> getAllCommunities(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    public Page<CommunityMembership> getMembers(UUID communityId, CommunityRole role, Pageable pageable) {
        Community community = getById(communityId);
        return membershipRepository.findByCommunityAndRoleWithMember(community, role, pageable);
    }

}
