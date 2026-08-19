package app.user.controller;

import app.common.mapper.DtoMapper;
import app.security.UserPrincipal;
import app.user.dto.UpdateAvatarResponse;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile/avatar")
    public ResponseEntity<UpdateAvatarResponse> updateAvatar(@AuthenticationPrincipal UserPrincipal principal,
                                                             @RequestParam("file") MultipartFile file) {
        User user = userService.updateAvatar(principal.getUserId(), file);
        UpdateAvatarResponse response = DtoMapper.toUpdateAvatarResponse(user);

        return ResponseEntity.ok(response);
    }
}
