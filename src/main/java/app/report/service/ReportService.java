package app.report.service;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.dto.PagedResponse;
import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ModerationServiceException;
import app.common.exception.ResourceNotFoundException;
import app.community.model.Community;
import app.community.service.CommunityService;
import app.post.model.Post;
import app.post.service.PostService;
import app.report.client.ModerationClient;
import app.report.client.dto.CreateReportRequest;
import app.report.client.dto.ReportResponse;
import app.report.client.dto.UpdateReportRequest;
import app.report.dto.ResolveReportRequest;
import app.report.dto.SubmitReportRequest;
import app.report.model.EnrichedReport;
import app.report.model.ReportStatus;
import app.report.model.TargetType;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ModerationClient moderationClient;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostService postService;
    private final CommentService commentService;

    public void reportPost(UUID postId, UUID reporterId, SubmitReportRequest request) {
        Post post = postService.getById(postId);
        User reporter = userService.getById(reporterId);

        if (communityService.isNotMember(post.getCommunity(), reporter)) {
            throw new ForbiddenOperationException("You must be a member of this community to report this post.");
        }

        if (post.getAuthor().getId().equals(reporterId)) {
            throw new BusinessRuleException("You cannot report your own post.");
        }

        CreateReportRequest clientRequest = CreateReportRequest.builder()
                .targetType(TargetType.POST)
                .targetId(postId)
                .communityId(post.getCommunity().getId())
                .reporterId(reporterId)
                .reason(request.getReason())
                .details(request.getDetails())
                .build();

        submitReport(clientRequest, postId, reporterId);
    }

    @Transactional
    public void reportComment(UUID commentId, UUID reporterId, SubmitReportRequest request) {
        Comment comment = commentService.getById(commentId);
        Community community = comment.getPost().getCommunity();
        User reporter = userService.getById(reporterId);

        if (communityService.isNotMember(community, reporter)) {
            throw new ForbiddenOperationException("You must be a member of this community to report this comment.");
        }

        if (comment.getAuthor().getId().equals(reporterId)) {
            throw new BusinessRuleException("You cannot report your own comment.");
        }

        CreateReportRequest clientRequest = CreateReportRequest.builder()
                .targetType(TargetType.COMMENT)
                .targetId(commentId)
                .communityId(community.getId())
                .reporterId(reporterId)
                .reason(request.getReason())
                .details(request.getDetails())
                .build();

        submitReport(clientRequest, commentId, reporterId);
    }

    public void resolveReport(UUID reportId, UUID communityId, UUID actingUserId, ResolveReportRequest request) {
        Community community = communityService.getById(communityId);
        User actingUser = userService.getById(actingUserId);

        communityService.requireModerator(community, actingUser, "Only the owner or moderators can resolve reports.");

        ReportStatus resolution = request.getStatus();
        if (resolution != ReportStatus.DISMISSED && resolution != ReportStatus.CONTENT_REMOVED) {
            throw new BusinessRuleException("A report can only be resolved as DISMISSED or CONTENT_REMOVED.");
        }

        ReportResponse response = updateReportStatus(reportId, resolution, actingUserId);

        if (resolution == ReportStatus.CONTENT_REMOVED) {
            removeReportedContent(response, actingUserId);
        }

        log.info("User with id [{}] resolved report with id [{}] as [{}].", actingUserId, reportId, resolution);
    }

    public Page<EnrichedReport> getReportsByCommunity(UUID communityId, UUID actingUserId,
                                                      ReportStatus status, Pageable pageable) {
        Community community = communityService.getById(communityId);
        User actingUser = userService.getById(actingUserId);

        communityService.requireModerator(community, actingUser, "Only the owner or moderators can view reports.");

        PagedResponse<ReportResponse> page = fetchReports(communityId, status, pageable);
        List<EnrichedReport> enrichedReports = page.getContent().stream()
                .map(this::buildEnrichedReport)
                .toList();


        log.info("{} reports fetched from moderation service for community [{}] by user [{}].",
                enrichedReports.size(), communityId, actingUserId);
        return new PageImpl<>(enrichedReports, pageable, page.getTotalElements());
    }

    private EnrichedReport buildEnrichedReport(ReportResponse report) {
        User reporter = userService.getById(report.getReporterId());
        User resolvedBy = report.getResolvedById() != null
                ? userService.getById(report.getResolvedById())
                : null;

        EnrichedReport.EnrichedReportBuilder builder = EnrichedReport.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdOn(report.getCreatedOn())
                .resolvedOn(report.getResolvedOn())
                .reporter(reporter)
                .resolvedBy(resolvedBy);

        try {
            if (report.getTargetType() == TargetType.POST) {
                Post post = postService.getById(report.getTargetId());
                builder.post(post);
            } else {
                Comment comment = commentService.getById(report.getTargetId());
                builder.comment(comment);
            }
            builder.contentAvailable(true);
        } catch (ResourceNotFoundException e) {
            builder.contentAvailable(false);
        }

        return builder.build();
    }

    private void removeReportedContent(ReportResponse report, UUID actingUserId) {
        try {
            if (report.getTargetType() == TargetType.POST) {
                postService.deletePost(report.getTargetId(), actingUserId);
            } else {
                commentService.removeReportedComment(report.getTargetId(), actingUserId);
            }
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            log.warn("Reported {} with id [{}] was already unavailable during removal: {}",
                    report.getTargetType().name().toLowerCase(), report.getTargetId(), e.getMessage());
        }
    }

    private ReportResponse updateReportStatus(UUID reportId, ReportStatus status, UUID resolvedById) {
        UpdateReportRequest request = UpdateReportRequest.builder()
                .status(status)
                .resolvedById(resolvedById)
                .build();

        try {
            return moderationClient.updateReport(reportId, request);
        } catch (HttpClientErrorException e) {
            log.warn("Moderation service rejected report resolution for id [{}] with status [{}].",
                    reportId, e.getStatusCode());
            throw new BusinessRuleException("This report cannot be resolved. It may not exist or has already been resolved.");
        } catch (RestClientException e) {
            log.error("Failed to reach moderation service while resolving report with id [{}] and status [{}].",
                    reportId, status, e);
            throw new ModerationServiceException("Unable to resolve report. Please try again later!");
        }
    }

    private PagedResponse<ReportResponse> fetchReports(UUID communityId, ReportStatus status, Pageable pageable) {
        try {
            return moderationClient.getReportsByCommunity(
                    communityId, status, pageable.getPageNumber(), pageable.getPageSize());
        } catch (RestClientException e) {
            log.error("Failed to fetch reports from moderation service for community with id [{}].", communityId, e);
            throw new ModerationServiceException("Unable to load reports. Please try again later!");
        }
    }

    private void submitReport(CreateReportRequest clientRequest, UUID targetId, UUID reporterId) {
        try {
            moderationClient.createReport(clientRequest);
            log.info("User with id [{}] reported {} with id [{}] for reason [{}].",
                    reporterId, clientRequest.getTargetType().name().toLowerCase(), targetId, clientRequest.getReason());

        } catch (RestClientException e) {
            log.error("Failed to submit report to moderation service for {} with id [{}].",
                    clientRequest.getTargetType().name(), targetId, e);
            throw new ModerationServiceException("Unable to submit report. Please try again later!");
        }
    }
}
