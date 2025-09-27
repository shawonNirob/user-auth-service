package com.multillm.auth.service;

import com.multillm.auth.dto.TeamInvitationDto;
import com.multillm.auth.mapper.TeamInvitationMapper;
import com.multillm.auth.model.AccountMember;
import com.multillm.auth.model.AccountMemberId;
import com.multillm.auth.model.TeamInvitation;
import com.multillm.auth.model.User;
import com.multillm.auth.model.enums.Role;
import com.multillm.auth.repository.AccountMemberRepository;
import com.multillm.auth.repository.TeamInvitationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamInvitationService {

    private final TeamInvitationMapper teamInvitationMapper;
    private final TeamInvitationRepository teamInvitationRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final EmailService emailService;

    @Transactional
    public void createInvitation(Long accountId, String inviteeEmail, Role role, User currentUser){
        log.info("Creating invitation for accountId: {} by userId: {}", accountId, currentUser.getId());
        AccountMember inviterMember = accountMemberRepository.findByAccountIdAndUserId(accountId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this team."));

        if(inviterMember.getRole() != Role.owner && inviterMember.getRole() != Role.admin){
            throw new AccessDeniedException("You do not have permission to invite members.");
        }

        TeamInvitation invitation = TeamInvitation.builder()
                .account(inviterMember.getAccount())
                .email(inviteeEmail)
                .role(role)
                .token(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .accepted(false)
                .build();
        teamInvitationRepository.save(invitation);

        String invitationUrl = "http://localhost:8080/api/v1/invitations/accept?token=" + invitation.getToken();
        emailService.send(inviteeEmail, "You’re invited!",
                "Click to join the team: \n" + invitationUrl + "\nExpires: " + invitation.getExpiresAt());
        log.debug("Invitation created with token for accountId: {}", accountId);
    }

    @Transactional
    public void acceptInvitation(String token, User user) {
        log.info("Accepting invitation token by userId: {}", user.getId());
        TeamInvitation invitation = teamInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid Invitation Token"));

        if(invitation.getExpiresAt().isBefore(OffsetDateTime.now())){
            throw new IllegalStateException("Invitation expired");
        }

        if(invitation.isAccepted()){
            throw new IllegalStateException("Invitation is already accepted");
        }

        AccountMember member = AccountMember.builder()
                .id(new AccountMemberId(invitation.getAccount().getId(), user.getId()))
                .account(invitation.getAccount())
                .user(user)
                .role(invitation.getRole())
                .build();
        accountMemberRepository.save(member);

        invitation.setAccepted(true);
        teamInvitationRepository.save(invitation);
        log.debug("Invitation accepted for accountId: {} by userId: {}", invitation.getAccount().getId(), user.getId());
    }

    //fetches all invitations for the account and maps them to DTOs
    public List<TeamInvitationDto> getInvitationsForAccount(Long accountId, User currentUser){
        accountMemberRepository.findByAccountIdAndUserId(accountId, currentUser.getId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("You are not a member of this team."));

        List<TeamInvitationDto> result = teamInvitationRepository.findByAccountId(accountId).stream()
                .map(teamInvitationMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Fetched {} invitations for accountId: {}", result.size(), accountId);
        return result;
    }
}
