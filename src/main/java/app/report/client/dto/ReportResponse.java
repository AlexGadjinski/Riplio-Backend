package app.report.client.dto;

import app.report.model.ReportReason;
import app.report.model.ReportStatus;
import app.report.model.TargetType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReportResponse {

    private UUID id;
    private TargetType targetType;
    private UUID targetId;
    private UUID communityId;
    private UUID reporterId;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private UUID resolvedById;
    private LocalDateTime createdOn;
    private LocalDateTime resolvedOn;
}
