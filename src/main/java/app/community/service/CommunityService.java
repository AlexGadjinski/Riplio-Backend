package app.community.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ResourceConflictException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;
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

    public Community updateAvatar(UUID communityId, UUID userId, MultipartFile file) {
        Community community = getById(communityId);

        if (!community.getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException("Only the community owner can update its avatar.");
        }

        fileValidator.validateImage(file);
        String avatarUrl = cloudinaryService.upload(file);

        community.setAvatar(avatarUrl);
        community.setUpdatedOn(LocalDateTime.now());
        return communityRepository.save(community);
    }

    public Community updateBanner(UUID communityId, UUID userId, MultipartFile file) {
        Community community = getById(communityId);

        if (!community.getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException("Only the community owner can update its banner.");
        }

        fileValidator.validateImage(file);
        String bannerUrl = cloudinaryService.upload(file);

        community.setBanner(bannerUrl);
        community.setUpdatedOn(LocalDateTime.now());
        return communityRepository.save(community);
    }

    public Community transferOwnership(UUID communityId, UUID currentOwnerId, UUID newOwnerId) {
        Community community = getById(communityId);

        if (!community.getOwner().getId().equals(currentOwnerId)) {
            throw new ForbiddenOperationException("Only the community owner can transfer ownership.");
        }

        if (currentOwnerId.equals(newOwnerId)) {
            throw new ResourceConflictException("You are already the owner of this community.");
        }

        User newOwner = userService.getById(newOwnerId);
        CommunityMembership targetMembership = membershipRepository.findByMemberAndCommunity(newOwner, community)
                .orElseThrow(() -> new BusinessRuleException("The new owner must be a member of this community."));

        if (targetMembership.getRole() != CommunityRole.MODERATOR) {
            throw new BusinessRuleException("The new owner must be a moderator of this community.");
        }

        community.setOwner(newOwner);
        community.setUpdatedOn(LocalDateTime.now());

        communityRepository.save(community);
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

    public CommunityMembership updateMember(UUID communityId, UUID actingUserId, UUID targetUserId, CommunityRole newRole) {
        Community community = getById(communityId);
        User targetUser = userService.getById(targetUserId);

        if (targetUserId.equals(community.getOwner().getId())) {
            throw new BusinessRuleException("The community owner's role cannot be changed. Transfer ownership first.");
        }

        CommunityMembership targetMembership = membershipRepository.findByMemberAndCommunity(targetUser, community)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User [%s] is not a member of this community.".formatted(targetUserId)));

        if (targetMembership.getRole() == newRole) {
            throw new ResourceConflictException("User already has [%s] role.".formatted(newRole));
        }

        boolean actingIsOwner = community.getOwner().getId().equals(actingUserId);
        boolean isPromotion = newRole == CommunityRole.MODERATOR;

        if (isPromotion) {
            User actingUser = userService.getById(actingUserId);
            boolean actingIsModerator = membershipRepository.existsByMemberAndCommunityAndRole(
                    actingUser, community, CommunityRole.MODERATOR);
            if (!actingIsModerator) {
                throw new ForbiddenOperationException("Only the owner or moderators can promote members.");
            }
        } else if (!actingIsOwner) {
            throw new ForbiddenOperationException("Only the owner can demote moderators.");
        }

        targetMembership.setRole(newRole);
        membershipRepository.save(targetMembership);

        targetMembership.setMember(targetUser);
        return targetMembership;
    }

    public void removeMember(UUID communityId, UUID actingUserId, UUID targetUserId) {
        Community community = getById(communityId);
        User targetUser = userService.getById(targetUserId);

        if (targetUserId.equals(community.getOwner().getId())) {
            throw new BusinessRuleException("The community owner cannot be removed. Transfer ownership first.");
        }

        CommunityMembership targetMembership = membershipRepository.findByMemberAndCommunity(targetUser, community)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User [%s] is not a member of this community.".formatted(targetUserId)));

        boolean isSelfRemoval = actingUserId.equals(targetUserId);

        if (!isSelfRemoval) {
            boolean actingIsOwner = community.getOwner().getId().equals(actingUserId);

            if (!actingIsOwner && targetMembership.getRole() == CommunityRole.MODERATOR) {
                throw new ForbiddenOperationException("Only the owner can remove a moderator.");
            }

            User actingUser = userService.getById(actingUserId);
            boolean actingIsModerator = membershipRepository.existsByMemberAndCommunityAndRole(
                    actingUser, community, CommunityRole.MODERATOR);

            if (!actingIsModerator) {
                throw new ForbiddenOperationException("Only the owner or moderators can remove members.");
            }
        }

        membershipRepository.delete(targetMembership);
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
