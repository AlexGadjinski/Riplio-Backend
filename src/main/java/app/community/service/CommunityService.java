package app.community.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ResourceConflictException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.community.dto.BanRequest;
import app.community.dto.UpsertCommunityRequest;
import app.community.model.Community;
import app.community.model.CommunityBan;
import app.community.model.CommunityMembership;
import app.community.model.CommunityRole;
import app.community.repository.CommunityBanRepository;
import app.community.repository.CommunityMembershipRepository;
import app.community.repository.CommunityRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final CommunityBanRepository banRepository;
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

        membershipRepository.save(initializeMembership(owner, community, CommunityRole.MODERATOR));

        log.info("Community with id [{}] created by user with id [{}].", community.getId(), ownerId);
        return community;
    }

    @Transactional
    public Community updateCommunityInfo(UUID communityId, UUID userId, UpsertCommunityRequest request) {
        Community community = getById(communityId);
        requireOwner(community, userId, "Only the community owner can update its info.");

        String newName = request.getName();
        if (!community.getName().equals(newName) && communityRepository.existsByName(newName)) {
            throw new ResourceConflictException("Community with name [%s] already exists.".formatted(newName));
        }

        community.setName(newName);
        community.setDescription(request.getDescription());
        community.setUpdatedOn(LocalDateTime.now());
        Community updatedCommunity = communityRepository.save(community);

        log.info("Community with id [{}] info updated by user with id [{}].", updatedCommunity.getId(), userId);
        return updatedCommunity;
    }

    public Community updateAvatar(UUID communityId, UUID userId, MultipartFile file) {
        Community community = getById(communityId);
        requireOwner(community, userId, "Only the community owner can update its avatar.");

        fileValidator.validateImage(file);
        String avatarUrl = cloudinaryService.upload(file);

        community.setAvatarUrl(avatarUrl);
        community.setUpdatedOn(LocalDateTime.now());
        Community updatedCommunity = communityRepository.save(community);

        log.info("Avatar updated for community with id [{}] by user with id [{}].", updatedCommunity.getId(), userId);
        return updatedCommunity;
    }

    public Community updateBanner(UUID communityId, UUID userId, MultipartFile file) {
        Community community = getById(communityId);
        requireOwner(community, userId, "Only the community owner can update its banner.");

        fileValidator.validateImage(file);
        String bannerUrl = cloudinaryService.upload(file);

        community.setBannerUrl(bannerUrl);
        community.setUpdatedOn(LocalDateTime.now());
        Community updatedCommunity = communityRepository.save(community);

        log.info("Banner updated for community with id [{}] by user with id [{}].", updatedCommunity.getId(), userId);
        return updatedCommunity;
    }

    public Community transferOwnership(UUID communityId, UUID currentOwnerId, UUID newOwnerId) {
        Community community = getById(communityId);
        requireOwner(community, currentOwnerId, "Only the community owner can transfer ownership.");

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

        log.info("Ownership of community with id [{}] transferred from user with id [{}] to user with id [{}].",
                communityId, currentOwnerId, newOwnerId);
        return community;
    }

    @Transactional
    public void deleteCommunity(UUID communityId, UUID userId) {
        Community community = getById(communityId);
        requireOwner(community, userId, "Only the community owner can delete this community.");

        banRepository.deleteByCommunity(community);
        membershipRepository.deleteByCommunity(community);
        communityRepository.delete(community);

        log.info("Community with id [{}] deleted by user with id [{}].", communityId, userId);
    }

    public CommunityMembership joinCommunity(UUID userId, UUID communityId) {
        Community community = getById(communityId);
        User member = userService.getById(userId);

        if (membershipRepository.existsByMemberAndCommunity(member, community)) {
            throw new ResourceConflictException("User [%s] is already a member of community [%s]."
                    .formatted(member.getUsername(), community.getName()));
        }

        if (banRepository.existsByBannedMemberAndCommunity(member, community)) {
            throw new ForbiddenOperationException("You are banned from this community.");
        }

        CommunityMembership membership = membershipRepository.save(initializeMembership(member, community, CommunityRole.MEMBER));

        log.info("User with id [{}] joined community with id [{}].", userId, communityId);
        return membership;
    }

    public CommunityMembership updateMember(UUID communityId, UUID actingUserId, UUID targetUserId, CommunityRole newRole) {
        Community community = getById(communityId);
        User actingUser = userService.getById(actingUserId);
        User targetUser = userService.getById(targetUserId);

        requireNotOwner(community, targetUserId, "The community owner's role cannot be changed. Transfer ownership first.");
        CommunityMembership targetMembership = requireMembership(community, targetUser);

        if (targetMembership.getRole() == newRole) {
            throw new ResourceConflictException("User already has [%s] role.".formatted(newRole));
        }

        boolean isPromotion = newRole == CommunityRole.MODERATOR;
        if (isPromotion) {
            requireModerator(community, actingUser, "Only the owner or moderators can promote members.");
        } else if (isNotOwner(community, actingUserId) && isNotAdmin(actingUser)) {
            throw new ForbiddenOperationException("Only the owner can demote moderators.");
        }

        targetMembership.setRole(newRole);
        membershipRepository.save(targetMembership);

        targetMembership.setMember(targetUser);

        log.info("User with id [{}] role changed to [{}] in community with id [{}] by user with id [{}].",
                targetUserId, newRole, communityId, actingUserId);
        return targetMembership;
    }

    public void removeMember(UUID communityId, UUID actingUserId, UUID targetUserId) {
        Community community = getById(communityId);
        User targetUser = userService.getById(targetUserId);

        requireNotOwner(community, targetUserId, "The community owner cannot be removed. Transfer ownership first.");
        CommunityMembership targetMembership = requireMembership(community, targetUser);

        boolean isSelfRemoval = actingUserId.equals(targetUserId);

        if (!isSelfRemoval) {
            User actingUser = userService.getById(actingUserId);

            if (isNotOwner(community, actingUserId) && targetMembership.getRole() == CommunityRole.MODERATOR
                    && isNotAdmin(actingUser)) {
                throw new ForbiddenOperationException("Only the owner can kick a moderator.");
            }

            requireModerator(community, actingUser, "Only the owner or moderators can kick members.");
        }

        membershipRepository.delete(targetMembership);


        log.info("User with id [{}] removed from community with id [{}] by user with id [{}].",
                targetUserId, communityId, actingUserId);
    }

    @Transactional
    public CommunityBan banMember(UUID communityId, UUID actingUserId, UUID targetUserId, BanRequest request) {
        Community community = getById(communityId);
        User targetUser = userService.getById(targetUserId);

        requireNotOwner(community, targetUserId, "The community owner cannot be banned. Transfer ownership first.");

        if (banRepository.existsByBannedMemberAndCommunity(targetUser, community)) {
            throw new ResourceConflictException(
                    "User with id [%s] is already banned from this community.".formatted(targetUserId));
        }

        CommunityMembership targetMembership = requireMembership(community, targetUser);
        User actingUser = userService.getById(actingUserId);

        if (isNotOwner(community, actingUserId) && targetMembership.getRole() == CommunityRole.MODERATOR && isNotAdmin(actingUser)) {
            throw new ForbiddenOperationException("Only the owner can ban a moderator.");
        }

        requireModerator(community, actingUser, "Only the owner or moderators can ban members.");

        membershipRepository.delete(targetMembership);

        CommunityBan ban = banRepository.save(initializeBan(targetUser, community, actingUser, request));

        log.info("User with id [{}] banned from community with id [{}] by user with id [{}].",
                targetUserId, communityId, actingUserId);

        return ban;
    }

    public void unbanMember(UUID communityId, UUID actingUserId, UUID targetUserId) {
        Community community = getById(communityId);
        User targetUser = userService.getById(targetUserId);

        User actingUser = userService.getById(actingUserId);
        requireModerator(community, actingUser, "Only the owner or moderators can unban members.");

        CommunityBan ban = banRepository.findByBannedMemberAndCommunity(targetUser, community)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] is not banned from this community.".formatted(targetUserId)));

        banRepository.delete(ban);

        log.info("User with id [{}] unbanned from community with id [{}] by user with id [{}].",
                targetUserId, communityId, actingUserId);
    }

    private void requireOwner(Community community, UUID userId, String message) {
        if (isNotOwner(community, userId) && isNotAdmin(userService.getById(userId))) {
            throw new ForbiddenOperationException(message);
        }
    }

    public void requireModerator(Community community, User user, String message) {
        if (isNotModerator(community, user) && isNotAdmin(user)) {
            throw new ForbiddenOperationException(message);
        }
    }

    private void requireNotOwner(Community community, UUID userId, String message) {
        if (userId.equals(community.getOwner().getId())) {
            throw new BusinessRuleException(message);
        }
    }

    private CommunityMembership requireMembership(Community community, User user) {
        return membershipRepository.findByMemberAndCommunity(user, community)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] is not a member of this community.".formatted(user.getId())));
    }

    private boolean isNotOwner(Community community, UUID userId) {
        return !community.getOwner().getId().equals(userId);
    }

    public boolean isNotModerator(Community community, User user) {
        return !membershipRepository.existsByMemberAndCommunityAndRole(user, community, CommunityRole.MODERATOR);
    }

    public boolean isNotMember(Community community, User user) {
        return !membershipRepository.existsByMemberAndCommunity(user, community);
    }

    private boolean isNotAdmin(User user) {
        return user.getRole() != UserRole.ADMIN;
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

    private CommunityMembership initializeMembership(User member, Community community, CommunityRole role) {
        return CommunityMembership.builder()
                .member(member)
                .community(community)
                .role(role)
                .joinedOn(LocalDateTime.now())
                .build();
    }

    private CommunityBan initializeBan(User bannedMember, Community community, User bannedBy, BanRequest request) {
        return CommunityBan.builder()
                .bannedMember(bannedMember)
                .community(community)
                .bannedBy(bannedBy)
                .reason(request.getReason())
                .bannedOn(LocalDateTime.now())
                .build();
    }

    public Community getById(UUID id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community with id [%s] does not exist.".formatted(id)));
    }

    public Page<CommunityBan> getBans(UUID communityId, UUID actingUserId, Pageable pageable) {
        Community community = getById(communityId);

        User actingUser = userService.getById(actingUserId);
        requireModerator(community, actingUser, "Only the owner or moderators can view banned members.");

        return banRepository.findByCommunityWithBannedMember(community, pageable);
    }

    public Page<Community> getAllCommunities(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    public Page<CommunityMembership> getMembers(UUID communityId, CommunityRole role, Pageable pageable) {
        Community community = getById(communityId);
        return membershipRepository.findByCommunityAndRoleWithMember(community, role, pageable);
    }

}
