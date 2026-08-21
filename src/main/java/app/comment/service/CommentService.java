package app.comment.service;

import app.comment.dto.UpsertCommentRequest;
import app.comment.model.Comment;
import app.comment.model.CommentStatus;
import app.comment.repository.CommentRepository;
import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.community.service.CommunityService;
import app.post.model.Post;
import app.post.service.PostService;
import app.user.model.User;
import app.user.service.UserService;
import com.cloudinary.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    public Comment createComment(UUID postId, UUID authorId, UpsertCommentRequest request) {
        Post post = postService.getById(postId);
        User author = userService.getById(authorId);

        if (!communityService.isMember(post.getCommunity(), author)) {
            throw new ForbiddenOperationException("You must be a member of this community to comment.");
        }

        MultipartFile image = request.getImage();
        boolean hasImage = StringUtils.isNotBlank(image);
        boolean hasContent = StringUtils.isNotBlank(request.getContent());

        if (!hasContent && !hasImage) {
            throw new BusinessRuleException("A comment must contain text, an image, or both.");
        }

        String imageUrl = null;
        if (hasImage) {
            fileValidator.validateImage(image);
            imageUrl = cloudinaryService.upload(image);
        }

        return commentRepository.save(initializeComment(request.getContent(), imageUrl, post, author, null));
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
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
