package app.moderation.client.dto;

import app.moderation.model.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UpdateReportRequest {

    private ReportStatus status;
    private UUID resolvedById;
}
