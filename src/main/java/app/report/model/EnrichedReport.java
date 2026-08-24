package app.report.model;

import app.comment.model.Comment;
import app.post.model.Post;
import app.user.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class EnrichedReport {

    private UUID id;
    private TargetType targetType;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private LocalDateTime createdOn;
    private LocalDateTime resolvedOn;

    private User reporter;
    private User resolvedBy;
    private Post post;
    private Comment comment;
    private boolean contentAvailable;
}
