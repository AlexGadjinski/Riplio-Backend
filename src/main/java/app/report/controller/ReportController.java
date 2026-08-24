package app.report.controller;

import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.report.dto.EnrichedReportResponse;
import app.report.dto.ResolveReportRequest;
import app.report.dto.SubmitReportRequest;
import app.report.model.ReportStatus;
import app.report.service.ReportService;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
                                           @Valid @RequestBody SubmitReportRequest request) {
        reportService.reportPost(postId, principal.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<Void> reportComment(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable UUID commentId,
                                              @Valid @RequestBody SubmitReportRequest request) {
        reportService.reportComment(commentId, principal.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/communities/{communityId}/reports/{id}")
    public ResponseEntity<Void> resolveReport(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable UUID communityId,
                                              @PathVariable UUID id,
                                              @Valid @RequestBody ResolveReportRequest request) {
        reportService.resolveReport(id, communityId, principal.getUserId(), request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/communities/{communityId}/reports")
    public ResponseEntity<PagedResponse<EnrichedReportResponse>> getReportsByCommunity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID communityId,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EnrichedReportResponse> enrichedReports = reportService.getReportsByCommunity(communityId, principal.getUserId(), status, pageable)
                .map(DtoMapper::toEnrichedReportResponse);
        PagedResponse<EnrichedReportResponse> response = PagedResponse.from(enrichedReports);

        return ResponseEntity.ok(response);
    }
}
