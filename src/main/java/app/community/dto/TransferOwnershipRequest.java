package app.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TransferOwnershipRequest {

    @NotNull(message = "New owner id is required")
    private UUID newOwnerId;
}
