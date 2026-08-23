package app.post.dto;

import app.post.model.PostMediaType;
import app.ripple.model.RippleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class ProfilePostResponse {

    private UUID id;
    private String title;
    private String content;
    private String mediaUrl;
    private PostMediaType mediaType;
    private UUID communityId;
    private String communityName;
    private String communityAvatarUrl;
    private int commentCount;
    private int rippleScore;
    private RippleType myRipple;
    private LocalDateTime createdOn;
}
