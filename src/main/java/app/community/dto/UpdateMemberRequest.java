package app.community.dto;

import app.community.model.CommunityRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateMemberRequest {

    @NotNull(message = "Role is required")
    private CommunityRole role;
}
