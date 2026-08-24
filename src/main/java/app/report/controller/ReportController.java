package app.report.controller;

import app.report.dto.ReportRequest;
import app.report.service.ReportService;
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
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<Void> reportPost(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable UUID postId,
                                           @Valid @RequestBody ReportRequest request) {
        reportService.reportPost(postId, principal.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<Void> reportComment(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable UUID commentId,
                                              @Valid @RequestBody ReportRequest request) {
        reportService.reportComment(commentId, principal.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
