package app.report.dto;

import app.report.model.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SubmitReportRequest {

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    @Size(max = 2000, message = "Details must not exceed 2000 characters")
    private String details;
}
