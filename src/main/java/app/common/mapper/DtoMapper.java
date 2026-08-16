package app.common.mapper;

import app.auth.dto.LoginResponse;
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
}
