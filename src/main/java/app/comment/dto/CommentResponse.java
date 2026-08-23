package app.comment.dto;

import app.comment.model.CommentStatus;
import app.ripple.model.RippleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CommentResponse {

    private UUID id;
    private String content;
    private String imageUrl;
    private String authorUsername;
    private String authorAvatarUrl;
    private UUID parentCommentId;
    private int replyCount;
    private int rippleScore;
    private RippleType myRipple;
    private CommentStatus status;
    private LocalDateTime createdOn;
}
