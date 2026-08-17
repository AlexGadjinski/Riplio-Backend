package app.community.controller;

import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.community.dto.CommunityListItemResponse;
import app.community.dto.CommunityMembershipResponse;
import app.community.dto.CommunityResponse;
import app.community.dto.CreateCommunityRequest;
import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.community.service.CommunityService;
import app.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/{id}/members")
    public ResponseEntity<CommunityMembershipResponse> joinCommunity(@AuthenticationPrincipal UserPrincipal principal,
                                                                     @PathVariable(name = "id") UUID communityId) {
        CommunityMembership membership = communityService.joinCommunity(principal.getUserId(), communityId);
        CommunityMembershipResponse response = DtoMapper.toCommunityMembershipResponse(membership);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable UUID id) {
        Community community = communityService.getByIdWithCreator(id);
        CommunityResponse response = DtoMapper.toCommunityResponse(community);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CommunityListItemResponse>> getAllCommunities(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CommunityListItemResponse> communities = communityService.getAll(pageable)
                .map(DtoMapper::toCommunityListItemResponse);
        PagedResponse<CommunityListItemResponse> response = PagedResponse.from(communities);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
