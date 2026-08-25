package app.user.service;

import app.common.exception.BusinessRuleException;
import app.common.exception.ResourceConflictException;
import app.common.exception.ResourceNotFoundException;
import app.common.storage.CloudinaryService;
import app.common.storage.FileValidator;
import app.user.dto.UpdateProfileRequest;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final CloudinaryService cloudinaryService;

    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getById(userId);

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new ResourceConflictException("Username [%s] is already taken.".formatted(request.getUsername()));
        }

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new ResourceConflictException("Email [%s] is already taken.".formatted(request.getEmail()));
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setUpdatedOn(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user with id [{}].", updatedUser.getId());

        return updatedUser;
    }

    public User updateAvatar(UUID userId, MultipartFile file) {
        fileValidator.validateImage(file);

        String avatarUrl = cloudinaryService.upload(file);

        User user = getById(userId);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedOn(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("Avatar updated for user with id [{}].", updatedUser.getId());

        return updatedUser;
    }

    public User promoteToAdmin(UUID userId) {
        User user = getById(userId);

        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessRuleException("User is already an admin.");
        }

        user.setRole(UserRole.ADMIN);
        user.setUpdatedOn(LocalDateTime.now());
        User promotedUser = userRepository.save(user);

        log.info("User with id [{}] promoted to admin.", promotedUser.getId());
        return promotedUser;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id [%s] does not exist.".formatted(id)));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username [%s] does not exist.".formatted(username)));
    }
}
