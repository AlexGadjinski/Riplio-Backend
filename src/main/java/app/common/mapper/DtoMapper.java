package app.common.mapper;

import app.auth.dto.LoginResponse;
import app.community.dto.*;
import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.security.jwt.GeneratedToken;
import app.user.dto.UpdateAvatarResponse;
import app.user.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static LoginResponse toLoginResponse(GeneratedToken generatedToken) {
        return LoginResponse.builder()
                .accessToken(generatedToken.getToken())
                .expiresAt(generatedToken.getExpiresAt())
                .build();
    }

    public static UpdateAvatarResponse toUpdateAvatarResponse(User user) {
        return new UpdateAvatarResponse(user.getProfilePicture());
    }

    public static CommunityResponse toCommunityResponse(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .avatar(community.getAvatar())
                .banner(community.getBanner())
                .createdOn(community.getCreatedOn())
                .build();
    }

    public static JoinCommunityResponse toJoinCommunityResponse(CommunityMembership membership) {
        return JoinCommunityResponse.builder()
                .communityId(membership.getCommunity().getId())
                .communityName(membership.getCommunity().getName())
                .role(membership.getRole())
                .joinedAt(membership.getJoinedAt())
                .build();
    }

    public static CommunityMemberResponse toCommunityMemberResponse(CommunityMembership membership) {
        User member = membership.getMember();

        return CommunityMemberResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .userImage(member.getProfilePicture())
                .role(membership.getRole())
                .joinedAt(membership.getJoinedAt())
                .build();
    }

}
