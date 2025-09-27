package com.multillm.auth.controller;

import com.multillm.auth.dto.AcceptInvitationRequestDto;
import com.multillm.auth.dto.CreateInvitationRequestDto;
import com.multillm.auth.dto.TeamInvitationDto;
import com.multillm.auth.model.User;
import com.multillm.auth.service.TeamInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;

    @PostMapping("/accounts/{accountId}/invitations")
    public ResponseEntity<Void> createInvitation(@PathVariable Long accountId,
                                                 @Valid @RequestBody CreateInvitationRequestDto request,
                                                 @AuthenticationPrincipal User user){

        log.info("Create invitation for accountId: {} by userId: {}", accountId, user.getId());
        teamInvitationService.createInvitation(accountId, request.getEmail(), request.getRole(), user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/accounts/{accountId}/invitations")
    public ResponseEntity<List<TeamInvitationDto>> getInvitations(@PathVariable Long accountId, @AuthenticationPrincipal User user){
        log.debug("Get invitations for accountId: {} by userId: {}", accountId, user.getId());
        return ResponseEntity.ok(teamInvitationService.getInvitationsForAccount(accountId, user));
    }

    //Bug - needed RequestParam Token
    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitation(@Valid @RequestBody AcceptInvitationRequestDto request, @AuthenticationPrincipal User user){
        log.info("Accept invitation requested by userId: {}", user.getId());
        teamInvitationService.acceptInvitation(request.getInvitationToken(), user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

















