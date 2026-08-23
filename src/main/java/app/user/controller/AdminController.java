package app.user.controller;

import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.user.dto.AdminUserResponse;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> promoteToAdmin(@PathVariable UUID id) {
        User user = userService.promoteToAdmin(id);
        AdminUserResponse response = DtoMapper.toAdminUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AdminUserResponse>> getAllUsers(Pageable pageable) {
        Page<AdminUserResponse> users = userService.getAllUsers(pageable)
                .map(DtoMapper::toAdminUserResponse);
        PagedResponse<AdminUserResponse> response = PagedResponse.from(users);

        return ResponseEntity.ok(response);
    }
}
