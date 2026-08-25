package app.ripple.service;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.model.Rippleable;
import app.community.service.CommunityService;
import app.post.model.Post;
import app.post.service.PostService;
import app.ripple.dto.RippleRequest;
import app.ripple.dto.RippleResponse;
import app.ripple.model.CommentRipple;
import app.ripple.model.PostRipple;
import app.ripple.model.Ripple;
import app.ripple.model.RippleType;
import app.ripple.repository.CommentRippleRepository;
import app.ripple.repository.PostRippleRepository;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RippleService {

    private final PostRippleRepository postRippleRepository;
    private final CommentRippleRepository commentRippleRepository;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostService postService;
    private final CommentService commentService;

    @Transactional
    public RippleResponse ripplePost(UUID postId, UUID actingUserId, RippleRequest request) {
        Post post = postService.getById(postId);
        User actingUser = userService.getById(actingUserId);

        if (post.getAuthor().getId().equals(actingUserId)) {
            throw new BusinessRuleException("You cannot ripple your own post.");
        }
        if (communityService.isNotMember(post.getCommunity(), actingUser)) {
            throw new ForbiddenOperationException("You must be a member of this community to ripple this post.");
        }

        Optional<PostRipple> optionalRipple = postRippleRepository.findByPostAndAuthor(post, actingUser);
        RippleType resultType = applyRipple(optionalRipple, request.getType(), post,
                () -> initializePostRipple(post, actingUser, request.getType()),
                postRippleRepository::save,
                postRippleRepository::delete);

        log.info("User with id [{}] rippled post with id [{}] with ripple type [{}].",
                actingUserId, postId, resultType);

        return RippleResponse.builder()
                .type(resultType)
                .score(post.getRippleScore())
                .build();
    }

    @Transactional
    public RippleResponse rippleComment(UUID commentId, UUID actingUserId, RippleRequest request) {
        Comment comment = commentService.getById(commentId);
        User actingUser = userService.getById(actingUserId);

        if (!comment.isActive()) {
            throw new BusinessRuleException("You can only ripple active comments.");
        }
        if (comment.getAuthor().getId().equals(actingUserId)) {
            throw new BusinessRuleException("You cannot ripple your own comment.");
        }
        if (communityService.isNotMember(comment.getPost().getCommunity(), actingUser)) {
            throw new ForbiddenOperationException("You must be a member of this community to ripple this comment.");
        }

        Optional<CommentRipple> optionalRipple = commentRippleRepository.findByCommentAndAuthor(comment, actingUser);
        RippleType resultType = applyRipple(optionalRipple, request.getType(), comment,
                () -> initializeCommentRipple(comment, actingUser, request.getType()),
                commentRippleRepository::save,
                commentRippleRepository::delete);

        log.info("User with id [{}] rippled comment with id [{}] with ripple type [{}].",
                actingUserId, commentId, resultType);

        return RippleResponse.builder()
                .type(resultType)
                .score(comment.getRippleScore())
                .build();
    }

    public RippleType getMyPostRipple(Post post, UUID viewingUserId) {
        User viewingUser = userService.getById(viewingUserId);

        return postRippleRepository.findByPostAndAuthor(post, viewingUser)
                .map(Ripple::getType)
                .orElse(null);
    }

    public Map<UUID, RippleType> getMyPostRipples(List<Post> posts, UUID viewingUserId) {
        User viewingUser = userService.getById(viewingUserId);

        return postRippleRepository.findByPostInAndAuthor(posts, viewingUser).stream()
                .collect(Collectors.toMap(r -> r.getPost().getId(), Ripple::getType));
    }

    public RippleType getMyCommentRipple(Comment comment, UUID viewingUserId) {
        User viewingUser = userService.getById(viewingUserId);

        return commentRippleRepository.findByCommentAndAuthor(comment, viewingUser)
                .map(Ripple::getType)
                .orElse(null);
    }

    public Map<UUID, RippleType> getMyCommentRipples(List<Comment> comments, UUID viewingUserId) {
        User viewingUser = userService.getById(viewingUserId);

        return commentRippleRepository.findByCommentInAndAuthor(comments, viewingUser).stream()
                .collect(Collectors.toMap(r -> r.getComment().getId(), Ripple::getType));
    }

    private <R extends Ripple> RippleType applyRipple(Optional<R> optionalRipple,
                                                      RippleType requestedType,
                                                      Rippleable target,
                                                      Supplier<R> newRippleSupplier,
                                                      Consumer<R> saveRipple,
                                                      Consumer<R> deleteRipple) {
        if (optionalRipple.isEmpty()) {
            saveRipple.accept(newRippleSupplier.get());
            updateRippleScore(target, requestedType == RippleType.RISE ? 1 : -1);
            return requestedType;
        }

        R ripple = optionalRipple.get();
        if (ripple.getType() == requestedType) {
            deleteRipple.accept(ripple);
            updateRippleScore(target, requestedType == RippleType.RISE ? -1 : 1);
            return null;
        } else {
            ripple.setType(requestedType);
            ripple.setUpdatedOn(LocalDateTime.now());
            saveRipple.accept(ripple);
            updateRippleScore(target, requestedType == RippleType.RISE ? 2 : -2);
            return requestedType;
        }
    }

    private void updateRippleScore(Rippleable target, int amount) {
        int steps = Math.abs(amount);
        for (int i = 0; i < steps; i++) {
            if (amount > 0) {
                target.incrementRippleScore();
            } else {
                target.decrementRippleScore();
            }
        }
    }

    private PostRipple initializePostRipple(Post post, User author, RippleType type) {
        LocalDateTime now = LocalDateTime.now();

        return PostRipple.builder()
                .post(post)
                .author(author)
                .type(type)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    private CommentRipple initializeCommentRipple(Comment comment, User author, RippleType type) {
        LocalDateTime now = LocalDateTime.now();

        return CommentRipple.builder()
                .comment(comment)
                .author(author)
                .type(type)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
