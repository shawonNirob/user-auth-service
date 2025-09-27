package com.multillm.auth.controller;


import com.multillm.auth.dto.*;
import com.multillm.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("Register request for email: {}", registerRequestDto.getEmail());

        authService.register(registerRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {

        log.info("Login attempt for email: {}", loginRequestDto.getEmail());

        AuthResponseDto authResponse = authService.login(loginRequestDto);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh-token")
                .maxAge(Duration.ofDays(365))
                .sameSite("Strict") //protect against CSRF
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body( new AuthResponseDto(
                        authResponse.getAccessToken(),
                        null,
                        authResponse.getUser()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        String accessToken = null;
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }
        log.info("Logout requested");
        authService.logout(accessToken, refreshTokenRequestDto.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        log.debug("Refresh token requested");
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequestDto.getRefreshToken()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token")  String token){
        log.debug("Email verification requested");
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email Verification Successful!");
    }
}

























