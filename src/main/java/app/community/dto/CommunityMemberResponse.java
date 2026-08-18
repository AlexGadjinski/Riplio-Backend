package app.community.dto;

import app.community.model.CommunityRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CommunityMemberResponse {

    private UUID memberId;
    private String username;
    private String userImage;
    private CommunityRole role;
    private LocalDateTime joinedAt;
}
