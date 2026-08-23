package app.user.dto;

import app.user.model.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class AdminUserResponse {

    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private UserRole role;
    private LocalDateTime createdOn;
}
