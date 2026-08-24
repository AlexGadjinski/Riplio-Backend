package app.moderation.client.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReportResponse {

    private UUID id;
    private String targetType;
    private UUID targetId;
    private UUID communityId;
    private UUID reporterId;
    private String reason;
    private String details;
    private String status;
    private UUID resolvedById;
    private LocalDateTime createdOn;
    private LocalDateTime resolvedOn;
}
