package app.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class BanResponse {

    private UUID communityId;
    private String bannedUsername;
    private LocalDateTime bannedOn;
}
