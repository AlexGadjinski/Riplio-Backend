package app.report.dto;

import app.comment.dto.CommentResponse;
import app.post.dto.PostSummaryResponse;
import app.report.model.ReportReason;
import app.report.model.ReportStatus;
import app.report.model.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class EnrichedReportResponse {

    private UUID id;
    private TargetType targetType;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private LocalDateTime createdOn;
    private LocalDateTime resolvedOn;

    private UUID reporterId;
    private String reporterUsername;
    private String reporterAvatarUrl;

    private UUID resolvedById;
    private String resolvedByUsername;
    private String resolvedByAvatarUrl;

    private PostSummaryResponse post;
    private CommentResponse comment;

    private boolean contentAvailable;
}
