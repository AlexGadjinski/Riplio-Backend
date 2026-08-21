package app.post.dto;

import app.post.model.PostMediaType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class PostResponse {

    private UUID id;
    private String title;
    private String content;
    private String mediaUrl;
    private PostMediaType mediaType;
    private UUID communityId;
    private String authorUsername;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
