package app.community.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpdateCommunityBannerResponse {

    private String bannerUrl;
}
