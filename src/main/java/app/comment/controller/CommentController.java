package app.comment.controller;

import app.comment.dto.CommentResponse;
import app.comment.dto.CreateCommentRequest;
import app.comment.dto.ProfileCommentResponse;
import app.comment.dto.UpdateCommentRequest;
import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID postId,
                                                         @Valid @ModelAttribute CreateCommentRequest request) {
        Comment comment = commentService.createComment(postId, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(comment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getTopLevelComments(
            @PathVariable UUID postId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CommentResponse> comments = commentService.getTopLevelComments(postId, pageable)
                .map(DtoMapper::toCommentResponse);
        PagedResponse<CommentResponse> response = PagedResponse.from(comments);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID id,
                                                         @ModelAttribute UpdateCommentRequest request) {
        Comment comment = commentService.updateComment(id, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(comment);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> deleteComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID id) {
        commentService.deleteComment(id, principal.getUserId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{id}/replies")
    public ResponseEntity<CommentResponse> createReply(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable(name = "id") UUID parentCommentId,
                                                       @Valid @ModelAttribute CreateCommentRequest request) {
        Comment reply = commentService.createReply(parentCommentId, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(reply);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/comments/{id}/replies")
    public ResponseEntity<PagedResponse<CommentResponse>> getReplies(
            @PathVariable(name = "id") UUID commentId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CommentResponse> replies = commentService.getReplies(commentId, pageable)
                .map(DtoMapper::toCommentResponse);
        PagedResponse<CommentResponse> response = PagedResponse.from(replies);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{id}/thread")
    public ResponseEntity<List<CommentResponse>> getCommentThread(@PathVariable UUID id) {
        List<CommentResponse> thread = commentService.getCommentThread(id).stream()
                .map(DtoMapper::toCommentResponse)
                .toList();

        return ResponseEntity.ok(thread);
    }

    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<PagedResponse<ProfileCommentResponse>> getCommentsByAuthor(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProfileCommentResponse> comments = commentService.getCommentsByAuthor(userId, pageable)
                .map(DtoMapper::toProfileCommentResponse);
        PagedResponse<ProfileCommentResponse> response = PagedResponse.from(comments);

        return ResponseEntity.ok(response);
    }
}
