package com.multillm.auth.service;

import com.multillm.auth.dto.AuthResponseDto;
import com.multillm.auth.dto.LoginRequestDto;
import com.multillm.auth.dto.RefreshTokenResponseDto;
import com.multillm.auth.dto.RegisterRequestDto;
import com.multillm.auth.mapper.AuthMapper;
import com.multillm.auth.mapper.UserMapper;
import com.multillm.auth.model.*;
import com.multillm.auth.repository.*;
import com.multillm.auth.model.enums.AccountType;
import com.multillm.auth.model.enums.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final TeamInvitationRepository invitationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final EmailService emailService;
    private final AccessTokenBlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;

    //register
    @Transactional
    public void register(RegisterRequestDto request) {
        log.info("Registering user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed, email already exists: {}", request.getEmail());
            throw new IllegalStateException("Email already exists");
        }

        // 1. Map DTO to User entity
        User user = authMapper.toUser(request);
        // 2. Manually encode and set the password
        log.info("Password is", user.getPassword());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        log.info("Password Hash is", user.getPasswordHash());
        userRepository.save(user);
        log.debug("Saved new user with id: {}", user.getId());

        //3. Create personal account and membership
        createPersonalAccountForUser(user);


        //4. Automatically join teams if invited
        //A Users should only be added to a team if they click the invitation link,
        // not just because they signed up with the same email.
        //processPendingInvitationsForUser(user);

        sendVerificationEmail(user);
        log.info("Verification email initiated for: {}", user.getEmail());

    }

    //Generate email verification token and send email
    private void sendVerificationEmail(User user) {
        String rawToken = UUID.randomUUID().toString();
        EmailVerificationToken tokenEntity = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(rawToken))
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .used(false)
                .build();
        emailVerificationTokenRepository.save(tokenEntity);
        log.debug("Saved email verification token for userId: {}", user.getId());

        //Send email for verification
        String verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + rawToken;
        emailService.send(user.getEmail(), "Verify Email", "Click the link to verify your email: " + verificationUrl);
    }

    //login
    public AuthResponseDto login(LoginRequestDto request){
        log.info("Login attempt for email: {}", request.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        log.info("Password is", request.getPassword());

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()->new UsernameNotFoundException("User not found"));

        // No second query needed. Get the user object directly.
        //User user = (User) authentication.getPrincipal();


        String accessToken = jwtService.generateAccessToken(user);
        //String refreshToken = jwtService.generateRefreshToken(user);

        //create opaque refresh token:
        String refreshToken = refreshTokenService.createRefreshToken(user, Duration.ofDays(1));

        //Don't need to save
        //saveUserRefreshToken(user, refreshToken);
        log.debug("Generated tokens for userId: {}", user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    //logout
    @Transactional
    public void logout(String rawAccessToken, String rawRefreshToken){
        log.info("Logout attempt");
        String[] parts = rawRefreshToken == null ? new String[0] : rawRefreshToken.split("\\.", 2);

        if(parts.length == 2){
            try{
                UUID sessionId = UUID.fromString(parts[0]);
                refreshTokenService.revokeSessionById(sessionId);
                log.debug("Revoked refresh sessionId: {}", sessionId);
            }catch(IllegalArgumentException ignored){}
        }

        //Old JWT refresh token
//        String userEmail = jwtService.extractUsername(rawRefreshToken);
//        User currentUser = this.userRepository.findByEmail(userEmail)
//                .orElseThrow(()->new UsernameNotFoundException("User not found"));
//
//        refreshTokenRepository.findAllByUser(currentUser)
//                .stream()
//                .filter(rt -> passwordEncoder.matches(rawRefreshToken, rt.getTokenHash()))
//                .findFirst()
//                .ifPresent(rt -> {
//                    rt.setRevoked(true);
//                    refreshTokenRepository.save(rt);
//                });

        //Blacklist the access token
        String jti = jwtService.extractTokenId(rawAccessToken);
        long expiryMills = jwtService.extractExpiration(rawAccessToken).getTime() - System.currentTimeMillis();
        if(expiryMills > 0) {
            blacklistService.blacklist(jti, expiryMills);
            log.debug("Blacklisted access token jti: {} for {} ms", jti, expiryMills);
        }

    }

    // Jwt refresh token
//    public RefreshTokenResponseDto refreshToken(String rawRefreshToken){
//        String userEmail = jwtService.extractUsername(rawRefreshToken);
//        User user = this.userRepository.findByEmail(userEmail)
//                .orElseThrow(()->new UsernameNotFoundException("User not found"));
//
//        // Find and validate the refresh token from DB
//        RefreshToken refreshTokenEntity = refreshTokenRepository.findAllByUser(user).stream()
//                .filter(rt -> passwordEncoder.matches(rawRefreshToken, rt.getTokenHash()))
//                .findFirst()
//                .orElseThrow(() -> new RefreshTokenException(rawRefreshToken, "Refresh Token Not found in DB!"));
//
//        if(refreshTokenEntity.isRevoked() || refreshTokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())){
//            throw new RefreshTokenException(rawRefreshToken, "Refresh token was expired or revoked. Please make a new sign in request");
//        }
//
//        String accessToken = jwtService.generateAccessToken(user);
//        return new RefreshTokenResponseDto(accessToken, rawRefreshToken);
//    }


    //opaque refresh token
    public RefreshTokenResponseDto refreshToken(String rawRefreshToken){
        log.debug("Attempting to refresh access token");
        Optional<RefreshToken> validateToken = refreshTokenService.validateRefreshToken(rawRefreshToken);

        RefreshToken refreshTokenEntity = validateToken
                .orElseThrow(() -> new IllegalStateException("Invalid or expired refresh token"));

        // Issue new access token
        String accessToken = jwtService.generateAccessToken(refreshTokenEntity.getUser());

        // Rotate refresh token
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenEntity, Duration.ofDays(1));

        log.debug("Rotated refresh token for userId: {}", refreshTokenEntity.getUser().getId());
        return new RefreshTokenResponseDto(accessToken, newRefreshToken);
    }


    //verify email
    @Transactional
    public void verifyEmail(String rawToken){
        log.debug("Verifying email using token");
        EmailVerificationToken tokenEntity = emailVerificationTokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid verification token."));

        if(tokenEntity.isUsed() || tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())){
            throw new IllegalStateException("Token expired or already used");
        }

        User user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for userId: {}", user.getId());

        tokenEntity.setUsed(true);
        emailVerificationTokenRepository.save(tokenEntity);
    }

    //create personal account for user
    private void createPersonalAccountForUser(User user){
        Account personalAccount = Account.builder()
                .name(user.getName() + "'s Personal Account")
                .accountType(AccountType.personal)
                .owner(user)
                .build();
        accountRepository.save(personalAccount);
        log.debug("Created personal accountId: {} for userId: {}", personalAccount.getId(), user.getId());

        AccountMemberId memberId = new AccountMemberId(personalAccount.getId(), user.getId());
        AccountMember membership = AccountMember.builder()
                .id(memberId)
                .account(personalAccount)
                .user(user)
                .role(Role.owner)
                .build();
        accountMemberRepository.save(membership);
        log.debug("Added owner membership for accountId: {} userId: {}", personalAccount.getId(), user.getId());
    }

    //Save user refresh token -- for old JWT Refresh Token
    private void saveUserRefreshToken(User user, String refreshToken){
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(refreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);
    }

    private void processPendingInvitationsForUser(User user){
        List<TeamInvitation> pendingInvites = invitationRepository.findByEmailAndAcceptedIsFalseAndExpiresAtAfter(user.getEmail(), OffsetDateTime.now());
        for(TeamInvitation invite : pendingInvites){
            AccountMemberId memberId = new AccountMemberId(invite.getAccount().getId(), user.getId());
            AccountMember newTeamMembership = AccountMember.builder()
                    .id(memberId)
                    .account(invite.getAccount())
                    .user(user)
                    .role(invite.getRole())
                    .build();
            accountMemberRepository.save(newTeamMembership);

            invite.setAccepted(true);
            invitationRepository.save(invite);
        }
    }

}
