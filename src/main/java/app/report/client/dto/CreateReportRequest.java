package app.report.client.dto;

import app.report.model.ReportReason;
import app.report.model.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CreateReportRequest {

    private TargetType targetType;
    private UUID targetId;
    private UUID communityId;
    private UUID reporterId;
    private ReportReason reason;
    private String details;
}
