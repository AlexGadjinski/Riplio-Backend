package app.common.mapper;

import app.auth.dto.LoginResponse;
import app.comment.dto.CommentResponse;
import app.comment.model.Comment;
import app.community.dto.*;
import app.community.model.Community;
import app.community.model.CommunityBan;
import app.community.model.CommunityMembership;
import app.post.dto.PostResponse;
import app.post.dto.PostSummaryResponse;
import app.post.dto.ProfilePostResponse;
import app.post.model.Post;
import app.security.jwt.GeneratedToken;
import app.user.dto.UpdateAvatarResponse;
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

    public static UpdateAvatarResponse toUpdateAvatarResponse(User user) {
        return new UpdateAvatarResponse(user.getAvatarUrl());
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
                .memberId(membership.getMember().getId())
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
                .memberId(member.getId())
                .username(member.getUsername())
                .avatarUrl(member.getAvatarUrl())
                .role(membership.getRole())
                .joinedOn(membership.getJoinedOn())
                .build();
    }

    public static PostResponse toPostResponse(Post post) {
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
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static PostSummaryResponse toPostSummaryResponse(Post post) {
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
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static ProfilePostResponse toProfilePostResponse(Post post) {
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
                .createdOn(post.getCreatedOn())
                .build();
    }

    public static CommentResponse toCommentResponse(Comment comment) {
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
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .build();
    }

}
