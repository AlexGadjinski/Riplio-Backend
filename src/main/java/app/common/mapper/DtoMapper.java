package app.common.mapper;

import app.auth.dto.LoginResponse;
import app.community.dto.CommunityListItemResponse;
import app.community.dto.CommunityMembershipResponse;
import app.community.dto.CommunityResponse;
import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.security.jwt.GeneratedToken;
import app.user.dto.UpdateAvatarResponse;
import app.user.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static CommunityResponse toCommunityResponse(Community community) {
        User creator = community.getCreator();

        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .image(community.getImage())
                .creatorUsername(creator.getUsername())
                .creatorImage(creator.getProfilePicture())
                .createdOn(community.getCreatedOn())
                .build();
    }

    public static CommunityListItemResponse toCommunityListItemResponse(Community community) {
        return CommunityListItemResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .image(community.getImage())
                .build();
    }

    public static CommunityMembershipResponse toCommunityMembershipResponse(CommunityMembership membership) {
        return CommunityMembershipResponse.builder()
                .communityId(membership.getCommunity().getId())
                .communityName(membership.getCommunity().getName())
                .role(membership.getRole())
                .joinedAt(membership.getJoinedAt())
                .build();
    }

    public static LoginResponse toLoginResponse(GeneratedToken generatedToken) {
        return LoginResponse.builder()
                .accessToken(generatedToken.getToken())
                .expiresAt(generatedToken.getExpiresAt())
                .build();
    }

    public static UpdateAvatarResponse toUpdateAvatarResponse(User user) {
        return new UpdateAvatarResponse(user.getProfilePicture());
    }
}
