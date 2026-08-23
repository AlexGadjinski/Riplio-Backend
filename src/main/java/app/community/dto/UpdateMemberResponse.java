package app.community.dto;

import app.community.model.CommunityRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UpdateMemberResponse {

    private UUID userId;
    private String username;
    private CommunityRole role;
}
