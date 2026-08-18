package app.community.dto;

import app.community.model.CommunityRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class JoinCommunityResponse {

    private UUID communityId;
    private String communityName;
    private CommunityRole role;
    private LocalDateTime joinedAt;
}
