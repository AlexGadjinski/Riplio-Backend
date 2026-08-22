package app.user.service;

import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;

    public User updateAvatar(UUID userId, MultipartFile file) {
        fileValidator.validateImage(file);

        String avatarUrl = cloudinaryService.upload(file);

        User user = getById(userId);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedOn(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id [%s] does not exist.".formatted(id)));
    }
}
