package app.community.service;

import app.common.exception.ResourceConflictException;
import app.common.exception.ResourceNotFoundException;
import app.community.dto.CreateCommunityRequest;
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
    public Community createCommunity(UUID creatorId, CreateCommunityRequest request) {

        if (communityRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Community with name [%s] already exists.".formatted(request.getName()));
        }

        User creator = userService.getById(creatorId);

        Community community = communityRepository.save(
                initializeCommunity(request.getName(), request.getDescription(), creator));

        CommunityMembership membership = initializeCommunityMembership(creator, community, CommunityRole.MODERATOR);
        membershipRepository.save(membership);

        return community;
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

    public Page<Community> getAll(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    private Community initializeCommunity(String name, String description, User creator) {
        LocalDateTime now = LocalDateTime.now();

        return Community.builder()
                .name(name)
                .description(description)
                .creator(creator)
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

    public Community getByIdWithCreator(UUID id) {
        return communityRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community with id [%s] does not exist.".formatted(id)));
    }
}
