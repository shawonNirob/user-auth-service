package com.multillm.auth.service;

import com.multillm.auth.dto.AccountDto;
import com.multillm.auth.dto.CreateTeamRequestDto;
import com.multillm.auth.mapper.AccountMapper;
import com.multillm.auth.model.Account;
import com.multillm.auth.model.AccountMember;
import com.multillm.auth.model.AccountMemberId;
import com.multillm.auth.model.User;
import com.multillm.auth.model.enums.AccountType;
import com.multillm.auth.model.enums.Role;
import com.multillm.auth.repository.AccountMemberRepository;
import com.multillm.auth.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountDto createTeamAccount(CreateTeamRequestDto request, User owner) {
        log.info("Creating team account for ownerId: {}", owner.getId());
        // 1. Map DTO to Account entity
        Account teamAccount = accountMapper.toAccount(request);

        // 2. Set fields not included in the DTO
        teamAccount.setAccountType(AccountType.team);
        teamAccount.setOwner(owner);
        accountRepository.save(teamAccount);

        // 3. Add the creator as the owner in the account_members table
        AccountMemberId  memberId = new AccountMemberId(teamAccount.getId(), owner.getId());
        AccountMember membership = AccountMember.builder()
                .id(memberId)
                .account(teamAccount)
                .user(owner)
                .role(Role.owner)
                .build();
        accountMemberRepository.save(membership);

        // 4. Map the final entity back to a DTO for the response
        log.debug("Created team accountId: {} for ownerId: {}", teamAccount.getId(), owner.getId());
        return accountMapper.toDto(teamAccount);
    }

    public AccountDto getAccountDetails(Long accountId, User currentUser) {
        // 1. Fetch the account by accountId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 2. Check if the current user is the owner of the account
        if (!account.getOwner().equals(currentUser)) {
            throw new RuntimeException("You are not authorized to view this account.");
        }

        log.debug("Returning account details for accountId: {}", accountId);
        return accountMapper.toDto(account);
    }

    //delete only RBAC for owner
    public void deleteAccount(Long accountId, User currentUser) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getOwner().equals(currentUser)) {
            throw new RuntimeException("You are not authorized to delete this account.");
        }
        log.warn("Deleted accountId: {} by ownerId: {}", accountId, currentUser.getId());
    }
}
