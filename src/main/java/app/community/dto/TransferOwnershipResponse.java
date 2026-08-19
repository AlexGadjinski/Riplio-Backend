package app.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class TransferOwnershipResponse {

    private UUID communityId;
    private String newOwnerUsername;
}
