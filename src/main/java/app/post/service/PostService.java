package app.post.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.community.model.Community;
import app.community.service.CommunityService;
import app.post.dto.CreatePostRequest;
import app.post.model.Post;
import app.post.model.PostMediaType;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.user.service.UserService;
import com.cloudinary.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final CommunityService communityService;

    public Post createPost(UUID communityId, UUID authorId, CreatePostRequest request) {
        Community community = communityService.getById(communityId);
        User author = userService.getById(authorId);

        if (!communityService.isMember(community, author)) {
            throw new ForbiddenOperationException("You must be a member of this community to create a post.");
        }

        MultipartFile file = request.getFile();
        boolean hasMedia = StringUtils.isNotBlank(file);
        boolean hasContent = StringUtils.isNotBlank(request.getContent());

        if (!hasContent && !hasMedia) {
            throw new BusinessRuleException("A post must contain text, media, or both.");
        }

        String mediaUrl = null;
        PostMediaType mediaType = null;
        if (hasMedia) {
            String contentType = fileValidator.validateMedia(file);
            mediaUrl = cloudinaryService.upload(file);
            mediaType = PostMediaType.fromContentType(contentType);
        }

        return postRepository.save(
                initializePost(request.getTitle(), request.getContent(), mediaUrl, mediaType, community, author));
    }

    public void deletePost(UUID postId, UUID actingUserId) {
        Post post = getById(postId);

        boolean isAuthor = post.getAuthor().getId().equals(actingUserId);
        if (!isAuthor) {
            User actingUser = userService.getById(actingUserId);
            if (!communityService.isModerator(post.getCommunity(), actingUser)) {
                throw new ForbiddenOperationException("Only the author or a community moderator can delete this post.");
            }
        }

        postRepository.delete(post);
    }

    public Post getById(UUID id) {
        return postRepository.findByIdWithAuthorAndCommunity(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id [%s] does not exist.".formatted(id)));
    }

    public Page<Post> getPosts(UUID communityId, Pageable pageable) {
        Community community = communityService.getById(communityId);

        return postRepository.findByCommunityWithAuthor(community, pageable);
    }

    private Post initializePost(String title, String content, String mediaUrl, PostMediaType mediaType,
                                Community community, User author) {
        return Post.builder()
                .title(title)
                .content(content)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .community(community)
                .author(author)
                .commentCount(0)
                .rippleScore(0)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
