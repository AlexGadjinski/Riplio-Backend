package app.comment.controller;

import app.comment.dto.CommentResponse;
import app.comment.dto.CreateCommentRequest;
import app.comment.dto.ProfileCommentResponse;
import app.comment.dto.UpdateCommentRequest;
import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.ripple.model.RippleType;
import app.ripple.service.RippleService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final RippleService rippleService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID postId,
                                                         @Valid @ModelAttribute CreateCommentRequest request) {
        Comment comment = commentService.createComment(postId, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(comment, null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getTopLevelComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID postId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Comment> comments = commentService.getTopLevelComments(postId, pageable);

        return toCommentPageResponse(comments, principal.getUserId());
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID id,
                                                         @ModelAttribute UpdateCommentRequest request) {
        Comment comment = commentService.updateComment(id, principal.getUserId(), request);
        CommentResponse response = DtoMapper.toCommentResponse(comment, null);

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
        CommentResponse response = DtoMapper.toCommentResponse(reply, null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/comments/{id}/replies")
    public ResponseEntity<PagedResponse<CommentResponse>> getReplies(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(name = "id") UUID commentId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Comment> replies = commentService.getReplies(commentId, pageable);

        return toCommentPageResponse(replies, principal.getUserId());
    }

    @GetMapping("/comments/{id}/thread")
    public ResponseEntity<List<CommentResponse>> getCommentThread(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable UUID id) {

        List<Comment> thread = commentService.getCommentThread(id);
        Map<UUID, RippleType> myRipples = rippleService.getMyCommentRipples(thread, principal.getUserId());

        List<CommentResponse> response = thread.stream()
                .map(c -> DtoMapper.toCommentResponse(c, myRipples.get(c.getId())))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<PagedResponse<ProfileCommentResponse>> getCommentsByAuthor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Comment> comments = commentService.getCommentsByAuthor(userId, pageable);
        Map<UUID, RippleType> myRipples = rippleService.getMyCommentRipples(comments.getContent(), principal.getUserId());

        Page<ProfileCommentResponse> mapped = comments.map(c -> DtoMapper.toProfileCommentResponse(c, myRipples.get(c.getId())));
        PagedResponse<ProfileCommentResponse> response = PagedResponse.from(mapped);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<PagedResponse<CommentResponse>> toCommentPageResponse(Page<Comment> comments, UUID viewingUserId) {
        Map<UUID, RippleType> myRipples = rippleService.getMyCommentRipples(comments.getContent(), viewingUserId);

        Page<CommentResponse> mapped = comments.map(c -> DtoMapper.toCommentResponse(c, myRipples.get(c.getId())));
        PagedResponse<CommentResponse> response = PagedResponse.from(mapped);

        return ResponseEntity.ok(response);
    }
}
