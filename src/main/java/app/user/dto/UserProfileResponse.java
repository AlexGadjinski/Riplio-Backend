package app.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class UserProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private LocalDateTime createdOn;
}
