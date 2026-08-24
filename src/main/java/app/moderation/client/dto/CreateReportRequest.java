package app.moderation.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CreateReportRequest {

    private String targetType;
    private UUID targetId;
    private UUID communityId;
    private UUID reporterId;
    private String reason;
    private String details;
}
