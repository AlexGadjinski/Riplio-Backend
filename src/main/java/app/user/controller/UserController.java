package app.user.controller;

import app.common.mapper.DtoMapper;
import app.security.UserPrincipal;
import app.user.dto.PublicUserProfileResponse;
import app.user.dto.UpdateAvatarResponse;
import app.user.dto.UpdateProfileRequest;
import app.user.dto.UserProfileResponse;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getById(principal.getUserId());
        UserProfileResponse response = DtoMapper.toUserProfileResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                               @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(principal.getUserId(), request);
        UserProfileResponse response = DtoMapper.toUserProfileResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<UpdateAvatarResponse> updateMyAvatar(@AuthenticationPrincipal UserPrincipal principal,
                                                               @RequestParam("file") MultipartFile file) {
        User user = userService.updateAvatar(principal.getUserId(), file);
        UpdateAvatarResponse response = DtoMapper.toUpdateAvatarResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(@PathVariable String username) {
        User user = userService.getByUsername(username);
        PublicUserProfileResponse response = DtoMapper.toPublicUserProfileResponse(user);

        return ResponseEntity.ok(response);
    }
}
