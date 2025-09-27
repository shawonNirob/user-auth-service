package com.multillm.auth.controller;

import com.multillm.auth.dto.AccountDto;
import com.multillm.auth.dto.CreateTeamRequestDto;
import com.multillm.auth.model.User;
import com.multillm.auth.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/teams")
    public ResponseEntity<?> createTeamAccount(@Valid @RequestBody CreateTeamRequestDto request, @AuthenticationPrincipal User user) {
        log.info("Create team account requested by userId: {}", user.getId());
        AccountDto accountDto = accountService.createTeamAccount(request,user);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDto);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable("accountId") Long accountId, @AuthenticationPrincipal User user) {
        log.debug("Get account {} requested by userId: {}", accountId, user.getId());
        return ResponseEntity.ok(accountService.getAccountDetails(accountId, user));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("accountId") Long accountId, @AuthenticationPrincipal User user) {
        log.warn("Delete account {} requested by userId: {}", accountId, user.getId());
        accountService.deleteAccount(accountId, user);
        return ResponseEntity.noContent().build();
    }

}



















