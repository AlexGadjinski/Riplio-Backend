package app.post.controller;

import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.post.dto.*;
import app.post.model.Post;
import app.post.service.PostService;
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
public class PostController {

    private final PostService postService;
    private final RippleService rippleService;

    @PostMapping("/communities/{communityId}/posts")
    public ResponseEntity<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID communityId,
                                                   @Valid @ModelAttribute CreatePostRequest request) {
        Post post = postService.createPost(communityId, principal.getUserId(), request);
        PostResponse response = DtoMapper.toPostResponse(post, null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/communities/{communityId}/posts")
    public ResponseEntity<PagedResponse<CommunityPostResponse>> getPostsByCommunity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID communityId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Post> posts = postService.getPostsByCommunity(communityId, pageable);
        Map<UUID, RippleType> myRipples = rippleService.getMyPostRipples(posts.getContent(), principal.getUserId());

        Page<CommunityPostResponse> mapped = posts.map(p -> DtoMapper.toCommunityPostResponse(p, myRipples.get(p.getId())));
        PagedResponse<CommunityPostResponse> response = PagedResponse.from(mapped);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<PagedResponse<ProfilePostResponse>> getPostsByAuthor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Post> posts = postService.getPostsByAuthor(userId, pageable);
        Map<UUID, RippleType> myRipples = rippleService.getMyPostRipples(posts.getContent(), principal.getUserId());

        Page<ProfilePostResponse> mapped = posts.map(p -> DtoMapper.toProfilePostResponse(p, myRipples.get(p.getId())));
        PagedResponse<ProfilePostResponse> response = PagedResponse.from(mapped);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/trending")
    public ResponseEntity<List<TrendingPostResponse>> getTrendingPosts() {
        List<TrendingPostResponse> response = postService.getTrendingPosts().stream()
                .map(DtoMapper::toTrendingPostResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable UUID id) {
        Post post = postService.getById(id);
        RippleType myRipple = rippleService.getMyPostRipple(post, principal.getUserId());

        PostResponse response = DtoMapper.toPostResponse(post, myRipple);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable UUID id) {
        postService.deletePost(id, principal.getUserId());

        return ResponseEntity.noContent().build();
    }
}
