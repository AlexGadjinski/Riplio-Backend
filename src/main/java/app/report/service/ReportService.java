package app.report.service;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ModerationServiceException;
import app.community.model.Community;
import app.community.service.CommunityService;
import app.post.model.Post;
import app.post.service.PostService;
import app.report.client.ModerationClient;
import app.report.client.dto.CreateReportRequest;
import app.report.dto.ReportRequest;
import app.report.model.TargetType;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

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

    public void reportPost(UUID postId, UUID reporterId, ReportRequest request) {
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
    public void reportComment(UUID commentId, UUID reporterId, ReportRequest request) {
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
