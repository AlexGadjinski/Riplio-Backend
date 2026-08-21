package app.comment.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpsertCommentRequest {

    private String content;
    private MultipartFile image;
}
