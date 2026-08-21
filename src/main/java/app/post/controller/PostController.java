package app.post.controller;

import app.common.mapper.DtoMapper;
import app.post.dto.PostResponse;
import app.post.dto.UpsertPostRequest;
import app.post.model.Post;
import app.post.service.PostService;
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
public class PostController {

    private final PostService postService;

    @PostMapping("/communities/{communityId}/posts")
    public ResponseEntity<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID communityId,
                                                   @Valid @ModelAttribute UpsertPostRequest request) {
        Post post = postService.createPost(communityId, principal.getUserId(), request);
        PostResponse response = DtoMapper.toPostResponse(post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
