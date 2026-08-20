package app.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class BannedMemberResponse {

    private UUID communityId;
    private String bannedUsername;
    private String bannedAvatarUrl;
    private String bannedByUsername;
    private String bannedByAvatarUrl;
    private String reason;
    private LocalDateTime bannedOn;
}
