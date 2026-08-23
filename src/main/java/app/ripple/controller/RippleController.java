package app.ripple.controller;

import app.ripple.dto.RippleRequest;
import app.ripple.dto.RippleResponse;
import app.ripple.service.RippleService;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RippleController {

    private final RippleService rippleService;

    @PostMapping("/posts/{postId}/ripples")
    public ResponseEntity<RippleResponse> ripplePost(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable UUID postId,
                                                     @Valid @RequestBody RippleRequest request) {
        RippleResponse response = rippleService.ripplePost(postId, principal.getUserId(), request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}/ripples")
    public ResponseEntity<RippleResponse> rippleComment(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable UUID commentId,
                                                        @Valid @RequestBody RippleRequest request) {
        RippleResponse response = rippleService.rippleComment(commentId, principal.getUserId(), request);

        return ResponseEntity.ok(response);
    }
}
