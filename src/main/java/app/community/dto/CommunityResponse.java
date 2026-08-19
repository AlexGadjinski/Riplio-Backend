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
    private String avatar;
    private String banner;
    private LocalDateTime createdOn;
}
