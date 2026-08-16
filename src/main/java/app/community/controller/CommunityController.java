package app.community.controller;

import app.common.mapper.DtoMapper;
import app.community.dto.CommunityResponse;
import app.community.dto.CreateCommunityRequest;
import app.community.model.Community;
import app.community.service.CommunityService;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody CreateCommunityRequest request) {
        Community community = communityService.createCommunity(principal.getUserId(), request);
        CommunityResponse response = DtoMapper.toCommunityResponse(community);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
