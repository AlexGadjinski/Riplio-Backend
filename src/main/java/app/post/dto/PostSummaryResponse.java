package app.post.dto;

import app.post.model.PostMediaType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class PostSummaryResponse {

    private UUID id;
    private String title;
    private String content;
    private String mediaUrl;
    private PostMediaType mediaType;
    private String authorUsername;
    private String authorAvatarUrl;
    private int commentCount;
    private int rippleScore;
    private LocalDateTime createdOn;
}
