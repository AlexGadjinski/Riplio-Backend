package app.comment.service;

import app.comment.dto.CreateCommentRequest;
import app.comment.dto.UpdateCommentRequest;
import app.comment.model.Comment;
import app.comment.model.CommentStatus;
import app.comment.repository.CommentRepository;
import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.community.service.CommunityService;
import app.post.model.Post;
import app.post.service.PostService;
import app.user.model.User;
import app.user.service.UserService;
import com.cloudinary.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostService postService;

    @Transactional
    public Comment createComment(UUID postId, UUID authorId, CreateCommentRequest request) {
        Post post = postService.getById(postId);
        return createComment(post, authorId, null, request);
    }

    @Transactional
    public Comment createReply(UUID parentCommentId, UUID authorId, CreateCommentRequest request) {
        Comment parentComment = getById(parentCommentId);
        return createComment(parentComment.getPost(), authorId, parentComment, request);
    }

    private Comment createComment(Post post, UUID authorId, Comment parentComment, CreateCommentRequest request) {
        User author = userService.getById(authorId);

        if (communityService.isNotMember(post.getCommunity(), author)) {
            throw new ForbiddenOperationException("You must be a member of this community to comment.");
        }

        String imageUrl = resolveImageUrl(request.getFile(), null, false);
        requireContentOrFile(request.getContent(), imageUrl);

        Comment comment = initializeComment(request.getContent(), imageUrl, post, author, parentComment);
        post.incrementCommentCount();
        if (parentComment != null) {
            parentComment.incrementReplyCount();
        }

        return commentRepository.save(comment);
    }

    public Comment updateComment(UUID commentId, UUID actingUserId, UpdateCommentRequest request) {
        Comment comment = getById(commentId);

        if (!isAuthor(comment, actingUserId)) {
            throw new ForbiddenOperationException("Only the author can edit this comment.");
        }

        if (!comment.isActive()) {
            throw new BusinessRuleException("Only active comments can be edited.");
        }

        String imageUrl = resolveImageUrl(request.getFile(), comment.getImageUrl(), request.isRemoveFile());
        requireContentOrFile(request.getContent(), imageUrl);

        comment.setContent(request.getContent());
        comment.setImageUrl(imageUrl);
        comment.setUpdatedOn(LocalDateTime.now());

        commentRepository.save(comment);
        return comment;
    }

    private String resolveImageUrl(MultipartFile file, String existingUrl, boolean removeFile) {
        boolean hasFile = StringUtils.isNotBlank(file);

        if (hasFile && removeFile) {
            throw new BusinessRuleException("Cannot upload a new file and remove the file at the same time.");
        } else if (removeFile) {
            return null;
        } else if (hasFile) {
            fileValidator.validateImage(file);
            return cloudinaryService.upload(file);
        }
        return existingUrl;
    }

    private void requireContentOrFile(String content, String imageUrl) {
        if (StringUtils.isBlank(content) && StringUtils.isBlank(imageUrl)) {
            throw new BusinessRuleException("A comment must contain text, an image, or both.");
        }
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID actingUserId) {
        Comment comment = getById(commentId);

        if (!comment.isActive()) {
            throw new BusinessRuleException("Only active comments can be deleted.");
        }

        if (isAuthor(comment, actingUserId)) {
            comment.setStatus(CommentStatus.DELETED);
        } else {
            User actingUser = userService.getById(actingUserId);
            communityService.requireModerator(comment.getPost().getCommunity(),
                    actingUser, "Only the author or moderators can delete this comment.");
            comment.setStatus(CommentStatus.REMOVED);
        }

        comment.setUpdatedOn(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public Page<Comment> getTopLevelComments(UUID postId, Pageable pageable) {
        Post post = postService.getById(postId);

        return commentRepository.findTopLevelByPostWithAuthor(post, pageable);
    }

    public Page<Comment> getReplies(UUID commentId, Pageable pageable) {
        Comment parentComment = getById(commentId);

        return commentRepository.findByParentCommentWithAuthor(parentComment, pageable);
    }

    public Page<Comment> getCommentsByAuthor(UUID authorId, Pageable pageable) {
        User author = userService.getById(authorId);

        return commentRepository.findByAuthorWithPostAndCommunity(author, pageable);
    }

    public List<Comment> getCommentThread(UUID commentId) {
        List<Comment> thread = new ArrayList<>();
        Comment comment = getById(commentId);

        while (comment != null) {
            thread.add(comment);

            if (comment.getParentComment() == null) {
                break;
            }

            comment = getById(comment.getParentComment().getId());
        }

        Collections.reverse(thread);
        return thread;
    }

    public Comment getById(UUID id) {
        return commentRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id [%s] does not exist.".formatted(id)));
    }

    private boolean isAuthor(Comment comment, UUID userId) {
        return comment.getAuthor().getId().equals(userId);
    }

    private Comment initializeComment(String content, String imageUrl, Post post, User author, Comment parentComment) {
        LocalDateTime now = LocalDateTime.now();

        return Comment.builder()
                .content(content)
                .imageUrl(imageUrl)
                .post(post)
                .author(author)
                .parentComment(parentComment)
                .status(CommentStatus.ACTIVE)
                .replyCount(0)
                .rippleScore(0)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
