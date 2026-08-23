package app.user.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public User promoteToAdmin(UUID userId) {
        User user = getById(userId);

        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessRuleException("User is already an admin.");
        }

        user.setRole(UserRole.ADMIN);
        user.setUpdatedOn(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id [%s] does not exist.".formatted(id)));
    }
}
