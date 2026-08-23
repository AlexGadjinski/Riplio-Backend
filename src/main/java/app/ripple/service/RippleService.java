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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            throw new ForbiddenOperationException("You cannot ripple your own post.");
        }
        if (communityService.isNotMember(post.getCommunity(), actingUser)) {
            throw new BusinessRuleException("You must be a member of this community to ripple this post.");
        }

        Optional<PostRipple> optionalRipple = postRippleRepository.findByPostAndAuthor(post, actingUser);
        RippleType resultType = applyRipple(optionalRipple, request.getType(), post,
                () -> initializePostRipple(post, actingUser, request.getType()),
                postRippleRepository::save,
                postRippleRepository::delete);

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
            throw new ForbiddenOperationException("You cannot ripple your own comment.");
        }
        if (communityService.isNotMember(comment.getPost().getCommunity(), actingUser)) {
            throw new BusinessRuleException("You must be a member of this community to ripple this comment.");
        }

        Optional<CommentRipple> optionalRipple = commentRippleRepository.findByCommentAndAuthor(comment, actingUser);
        RippleType resultType = applyRipple(optionalRipple, request.getType(), comment,
                () -> initializeCommentRipple(comment, actingUser, request.getType()),
                commentRippleRepository::save,
                commentRippleRepository::delete);

        return RippleResponse.builder()
                .type(resultType)
                .score(comment.getRippleScore())
                .build();
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
