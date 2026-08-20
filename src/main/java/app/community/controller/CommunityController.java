package app.community.controller;

import app.common.dto.PagedResponse;
import app.common.mapper.DtoMapper;
import app.community.dto.*;
import app.community.model.Community;
import app.community.model.CommunityBan;
import app.community.model.CommunityMembership;
import app.community.model.CommunityRole;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody UpsertCommunityRequest request) {
        Community community = communityService.createCommunity(principal.getUserId(), request);
        CommunityResponse response = DtoMapper.toCommunityResponse(community);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponse> updateCommunityInfo(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable UUID id,
                                                                 @Valid @RequestBody UpsertCommunityRequest request) {
        Community community = communityService.updateCommunityInfo(id, principal.getUserId(), request);
        CommunityResponse response = DtoMapper.toCommunityResponse(community);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<UpdateCommunityAvatarResponse> updateAvatar(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @PathVariable UUID id,
                                                                      @RequestParam("file") MultipartFile file) {
        Community community = communityService.updateAvatar(id, principal.getUserId(), file);
        UpdateCommunityAvatarResponse response = DtoMapper.toUpdateCommunityAvatarResponse(community);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/banner")
    public ResponseEntity<UpdateCommunityBannerResponse> updateBanner(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @PathVariable UUID id,
                                                                      @RequestParam("file") MultipartFile file) {
        Community community = communityService.updateBanner(id, principal.getUserId(), file);
        UpdateCommunityBannerResponse response = DtoMapper.toUpdateCommunityBannerResponse(community);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/owner")
    public ResponseEntity<TransferOwnershipResponse> transferOwnership(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody TransferOwnershipRequest request) {

        Community community = communityService.transferOwnership(id, principal.getUserId(), request.getNewOwnerId());
        TransferOwnershipResponse response = DtoMapper.toTransferOwnershipResponse(community);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<JoinCommunityResponse> joinCommunity(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable UUID id) {
        CommunityMembership membership = communityService.joinCommunity(principal.getUserId(), id);
        JoinCommunityResponse response = DtoMapper.toJoinCommunityResponse(membership);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<UpdateMemberResponse> updateMember(@AuthenticationPrincipal UserPrincipal principal,
                                                             @PathVariable(name = "id") UUID communityId,
                                                             @PathVariable UUID userId,
                                                             @Valid @RequestBody UpdateMemberRequest request) {
        CommunityMembership membership = communityService.updateMember(
                communityId, principal.getUserId(), userId, request.getRole());
        UpdateMemberResponse response = DtoMapper.toUpdateMemberResponse(membership);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable(name = "id") UUID communityId,
                                             @PathVariable UUID userId) {
        communityService.removeMember(communityId, principal.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/bans/{userId}")
    public ResponseEntity<BanResponse> banMember(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable(name = "id") UUID communityId,
                                                 @PathVariable UUID userId,
                                                 @Valid @RequestBody BanRequest request) {
        CommunityBan communityBan = communityService.banMember(communityId, principal.getUserId(), userId, request);
        BanResponse response = DtoMapper.toBanResponse(communityBan);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}/bans/{userId}")
    public ResponseEntity<Void> unbanMember(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable(name = "id") UUID communityId,
                                            @PathVariable UUID userId) {
        communityService.unbanMember(communityId, principal.getUserId(), userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable UUID id) {
        Community community = communityService.getById(id);
        CommunityResponse response = DtoMapper.toCommunityResponse(community);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable UUID id) {
        communityService.deleteCommunity(id, principal.getUserId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CommunityResponse>> getAllCommunities(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CommunityResponse> communities = communityService.getAllCommunities(pageable)
                .map(DtoMapper::toCommunityResponse);
        PagedResponse<CommunityResponse> response = PagedResponse.from(communities);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<PagedResponse<CommunityMemberResponse>> getMembers(
            @PathVariable(name = "id") UUID communityId,
            @RequestParam(required = false) CommunityRole role,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CommunityMemberResponse> members = communityService.getMembers(communityId, role, pageable)
                .map(DtoMapper::toCommunityMemberResponse);
        PagedResponse<CommunityMemberResponse> response = PagedResponse.from(members);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/bans")
    public ResponseEntity<PagedResponse<BannedMemberResponse>> getBans(@AuthenticationPrincipal UserPrincipal principal,
                                                                       @PathVariable UUID id,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        Page<BannedMemberResponse> bans = communityService.getBans(id, principal.getUserId(), pageable)
                .map(DtoMapper::toBannedMemberResponse);
        PagedResponse<BannedMemberResponse> response = PagedResponse.from(bans);

        return ResponseEntity.ok(response);
    }

}
