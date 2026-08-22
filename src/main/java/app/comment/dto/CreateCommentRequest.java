package app.comment.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateCommentRequest {

    private String content;
    private MultipartFile file;
}
