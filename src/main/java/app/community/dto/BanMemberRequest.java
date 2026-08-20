package app.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class BanMemberRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}
