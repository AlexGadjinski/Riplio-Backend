package app.comment.dto;

import app.ripple.model.RippleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class ProfileCommentResponse {

    private UUID id;
    private UUID communityId;
    private String communityName;
    private String communityAvatarUrl;
    private UUID postId;
    private String postTitle;
    private String content;
    private String imageUrl;
    private int replyCount;
    private int rippleScore;
    private RippleType myRipple;
    private LocalDateTime createdOn;
}
