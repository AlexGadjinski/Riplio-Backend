package app.community.service;

import app.common.exception.ResourceConflictException;
import app.community.dto.CreateCommunityRequest;
import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.community.model.CommunityRole;
import app.community.repository.CommunityMembershipRepository;
import app.community.repository.CommunityRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
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
}
