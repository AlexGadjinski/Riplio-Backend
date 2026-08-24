package app.moderation.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UpdateReportRequest {

    private String status;
    private UUID resolvedById;
}
