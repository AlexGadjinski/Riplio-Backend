package app.report.dto;

import app.report.model.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ResolveReportRequest {

    @NotNull(message = "Resolution status is required")
    private ReportStatus status;
}
