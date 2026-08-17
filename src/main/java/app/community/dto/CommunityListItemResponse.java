package app.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CommunityListItemResponse {

    private UUID id;
    private String name;
    private String description;
    private String image;
}
