package app.post.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ForbiddenOperationException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.community.model.Community;
import app.community.service.CommunityService;
import app.post.dto.UpsertPostRequest;
import app.post.model.Post;
import app.post.model.PostMediaType;
import app.post.repository.PostRepository;
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
public class PostService {

    private final PostRepository postRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final CommunityService communityService;

    public Post createPost(UUID communityId, UUID authorId, UpsertPostRequest request) {
        Community community = communityService.getById(communityId);
        User author = userService.getById(authorId);

        if (!communityService.isMember(community, author)) {
            throw new ForbiddenOperationException("You must be a member of this community to create a post.");
        }

        MultipartFile media = request.getMedia();
        boolean hasMedia = StringUtils.isNotBlank(media);
        boolean hasContent = StringUtils.isNotBlank(request.getContent());

        if (!hasContent && !hasMedia) {
            throw new BusinessRuleException("A post must contain text, media or both.");
        }

        String mediaUrl = null;
        PostMediaType mediaType = null;
        if (hasMedia) {
            String contentType = fileValidator.validateMedia(media);
            mediaUrl = cloudinaryService.upload(media);
            mediaType = PostMediaType.fromContentType(contentType);
        }

        return postRepository.save(
                initializePost(request.getTitle(), request.getContent(), mediaUrl, mediaType, community, author));
    }

    private Post initializePost(String title, String content, String mediaUrl, PostMediaType mediaType,
                                Community community, User author) {
        LocalDateTime now = LocalDateTime.now();

        return Post.builder()
                .title(title)
                .content(content)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .community(community)
                .author(author)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
