package app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 6, max = 30, message = "Username must be between 6 and 30 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
