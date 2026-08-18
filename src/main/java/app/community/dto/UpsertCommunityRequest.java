package app.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpsertCommunityRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 6, max = 30, message = "Name must be between 6 and 30 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
