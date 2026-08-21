package app.comment.controller;

import app.comment.dto.CommentResponse;
import app.comment.dto.UpsertCommentRequest;
import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.mapper.DtoMapper;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID postId,
                                                         @Valid @ModelAttribute UpsertCommentRequest request) {
        Comment comment = commentService.createComment(postId, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(comment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
