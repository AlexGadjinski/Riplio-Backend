package app.common.mapper;

import app.auth.dto.LoginResponse;
import app.comment.dto.CommentResponse;
import app.comment.dto.ProfileCommentResponse;
import app.comment.model.Comment;
import app.community.dto.*;
import app.community.model.Community;
import app.community.model.CommunityBan;
import app.community.model.CommunityMembership;
import app.post.dto.PostResponse;
import app.post.dto.PostSummaryResponse;
import app.post.dto.ProfilePostResponse;
import app.post.model.Post;
import app.report.dto.EnrichedReportResponse;
import app.report.model.EnrichedReport;
import app.report.model.TargetType;
import app.ripple.model.RippleType;
import app.security.jwt.GeneratedToken;
import app.user.dto.AdminUserResponse;
import app.user.dto.PublicUserProfileResponse;
import app.user.dto.UpdateAvatarResponse;
import app.user.dto.UserProfileResponse;
import app.user.model.User;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class DtoMapper {

    private static final String DELETED_AUTHOR_PLACEHOLDER = "[deleted]";
    private static final String DELETED_CONTENT_TEXT = "[this comment self-destructed]";
    private static final String REMOVED_CONTENT_TEXT = "[zapped by a moderator]";

    public static LoginResponse toLoginResponse(GeneratedToken generatedToken) {
        return LoginResponse.builder()
                .accessToken(generatedToken.getToken())
                .expiresAt(generatedToken.getExpiresAt())
                .build();
    }

    public static UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .createdOn(user.getCreatedOn())
                .build();
    }

    public static PublicUserProfileResponse toPublicUserProfileResponse(User user) {
        return PublicUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .createdOn(user.getCreatedOn())
                .build();
    }

    public static UpdateAvatarResponse toUpdateAvatarResponse(User user) {
        return new UpdateAvatarResponse(user.getAvatarUrl());
    }

    public static AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdOn(user.getCreatedOn())
                .build();
    }

    public static CommunityResponse toCommunityResponse(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .avatarUrl(community.getAvatarUrl())
                .bannerUrl(community.getBannerUrl())
                .createdOn(community.getCreatedOn())
                .build();
    }

    public static UpdateCommunityAvatarResponse toUpdateCommunityAvatarResponse(Community community) {
        return UpdateCommunityAvatarResponse.builder()
                .avatarUrl(community.getAvatarUrl())
                .build();
    }

    public static UpdateCommunityBannerResponse toUpdateCommunityBannerResponse(Community community) {
        return UpdateCommunityBannerResponse.builder()
                .bannerUrl(community.getBannerUrl())
                .build();
    }

    public static TransferOwnershipResponse toTransferOwnershipResponse(Community community) {
        return TransferOwnershipResponse.builder()
                .communityId(community.getId())
                .newOwnerUsername(community.getOwner().getUsername())
                .build();
    }

    public static JoinCommunityResponse toJoinCommunityResponse(CommunityMembership membership) {
        return JoinCommunityResponse.builder()
                .communityId(membership.getCommunity().getId())
                .communityName(membership.getCommunity().getName())
                .role(membership.getRole())
                .joinedOn(membership.getJoinedOn())
                .build();
    }

    public static UpdateMemberResponse toUpdateMemberResponse(CommunityMembership membership) {
        return UpdateMemberResponse.builder()
                .userId(membership.getMember().getId())
                .username(membership.getMember().getUsername())
                .role(membership.getRole())
                .build();
    }

    public static BanResponse toBanResponse(CommunityBan communityBan) {
        return BanResponse.builder()
                .communityId(communityBan.getCommunity().getId())
                .bannedUsername(communityBan.getBannedMember().getUsername())
                .bannedOn(communityBan.getBannedOn())
                .build();
    }

    public static BannedMemberResponse toBannedMemberResponse(CommunityBan communityBan) {
        return BannedMemberResponse.builder()
                .communityId(communityBan.getCommunity().getId())
                .bannedUserId(communityBan.getBannedMember().getId())
                .bannedUsername(communityBan.getBannedMember().getUsername())
                .bannedAvatarUrl(communityBan.getBannedMember().getAvatarUrl())
                .bannedByUsername(communityBan.getBannedBy().getUsername())
                .bannedByAvatarUrl(communityBan.getBannedBy().getAvatarUrl())
                .reason(communityBan.getReason())
                .bannedOn(communityBan.getBannedOn())
                .build();
    }

    public static CommunityMemberResponse toCommunityMemberResponse(CommunityMembership membership) {
        User member = membership.getMember();

        return CommunityMemberResponse.builder()
                .userId(member.getId())
                .username(member.getUsername())
                .avatarUrl(member.getAvatarUrl())
                .role(membership.getRole())
                .joinedOn(membership.getJoinedOn())
                .build();
    }

    public static PostResponse toPostResponse(Post post, RippleType myRipple) {
        Community community = post.getCommunity();

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .mediaType(post.getMediaType())
                .communityId(community.getId())
                .communityName(community.getName())
                .communityAvatarUrl(community.getAvatarUrl())
                .authorUsername(post.getAuthor().getUsername())
                .commentCount(post.getCommentCount())
                .rippleScore(post.getRippleScore())
                .myRipple(myRipple)
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static PostSummaryResponse toPostSummaryResponse(Post post, RippleType myRipple) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .mediaType(post.getMediaType())
                .authorUsername(post.getAuthor().getUsername())
                .authorAvatarUrl(post.getAuthor().getAvatarUrl())
                .commentCount(post.getCommentCount())
                .rippleScore(post.getRippleScore())
                .myRipple(myRipple)
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static ProfilePostResponse toProfilePostResponse(Post post, RippleType myRipple) {
        Community community = post.getCommunity();

        return ProfilePostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .mediaType(post.getMediaType())
                .communityId(community.getId())
                .communityName(community.getName())
                .communityAvatarUrl(community.getAvatarUrl())
                .commentCount(post.getCommentCount())
                .rippleScore(post.getRippleScore())
                .myRipple(myRipple)
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static CommentResponse toCommentResponse(Comment comment, RippleType myRipple) {
        boolean showAuthor = comment.isActive() || comment.isRemoved();

        String authorUsername = showAuthor ? comment.getAuthor().getUsername() : DELETED_AUTHOR_PLACEHOLDER;
        String authorAvatarUrl = showAuthor ? comment.getAuthor().getAvatarUrl() : null;
        String content = comment.isActive()
                ? comment.getContent()
                : (comment.isDeleted() ? DELETED_CONTENT_TEXT : REMOVED_CONTENT_TEXT);
        String imageUrl = comment.isActive() ? comment.getImageUrl() : null;
        UUID parentCommentId = comment.getParentComment() == null ? null : comment.getParentComment().getId();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(content)
                .imageUrl(imageUrl)
                .authorUsername(authorUsername)
                .authorAvatarUrl(authorAvatarUrl)
                .parentCommentId(parentCommentId)
                .replyCount(comment.getReplyCount())
                .rippleScore(comment.getRippleScore())
                .myRipple(myRipple)
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .build();
    }

    public static ProfileCommentResponse toProfileCommentResponse(Comment comment, RippleType myRipple) {
        Post post = comment.getPost();
        Community community = post.getCommunity();

        return ProfileCommentResponse.builder()
                .id(comment.getId())
                .communityId(community.getId())
                .communityName(community.getName())
                .communityAvatarUrl(community.getAvatarUrl())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl())
                .replyCount(comment.getReplyCount())
                .myRipple(myRipple)
                .rippleScore(comment.getRippleScore())
                .createdOn(comment.getCreatedOn())
                .build();
    }

    public static EnrichedReportResponse toEnrichedReportResponse(EnrichedReport report) {
        User reporter = report.getReporter();

        EnrichedReportResponse.EnrichedReportResponseBuilder builder = EnrichedReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdOn(report.getCreatedOn())
                .resolvedOn(report.getResolvedOn())
                .reporterId(reporter.getId())
                .reporterUsername(reporter.getUsername())
                .reporterAvatarUrl(reporter.getAvatarUrl())
                .contentAvailable(report.isContentAvailable());

        User resolvedBy = report.getResolvedBy();
        if (resolvedBy != null) {
            builder.resolvedById(resolvedBy.getId())
                    .resolvedByUsername(resolvedBy.getUsername())
                    .resolvedByAvatarUrl(resolvedBy.getAvatarUrl());
        }

        if (report.getTargetType() == TargetType.POST && report.getPost() != null) {
            builder.post(toPostSummaryResponse(report.getPost(), null));
        } else {
            builder.comment(toCommentResponse(report.getComment(), null));
        }

        return builder.build();
    }

}
