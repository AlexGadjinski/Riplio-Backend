package app.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CommunityResponse {

    private UUID id;
    private String name;
    private String description;
    private String avatarUrl;
    private String bannerUrl;
    private LocalDateTime createdOn;
}
