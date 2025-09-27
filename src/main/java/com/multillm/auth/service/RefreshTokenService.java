package com.multillm.auth.service;

import com.multillm.auth.model.RefreshToken;
import com.multillm.auth.model.User;
import com.multillm.auth.repository.RefreshTokenRepository;
import com.multillm.auth.util.CryptoUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    //private final PasswordEncoder passwordEncoder;
    private final TokenEncryptionService tokenEncryptionService;

    public String createRefreshToken(User user, Duration ttl) {
        log.trace("Creating refresh token for userId: {}", user.getId());
        UUID sessionId = UUID.randomUUID();
        String secret = CryptoUtils.generateSecureRandomUrlSafeString(24);
        String refreshToken = sessionId.toString() + "." + secret;

        RefreshToken token = RefreshToken.builder()
                .sessionId(sessionId)
                .user(user)
                .tokenHash(tokenEncryptionService.hash(refreshToken))
                .expiresAt(OffsetDateTime.now().plus(ttl))
                .build();
        refreshTokenRepository.save(token);
        log.debug("Created refresh sessionId: {} for userId: {}", sessionId, user.getId());
        return refreshToken;
    }

    // validate: returns RefreshToken if valid (non-revoked, not expired, secret matches)
    public Optional<RefreshToken> validateRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.length() == 0) {
            log.debug("Refresh token is null");
            return Optional.empty();
        }

        String tokenInput = rawRefreshToken.trim();
        if (tokenInput.isEmpty()) {
            log.debug("Refresh token is blank after trim");
            return Optional.empty();
        }


        String[] parts = tokenInput.split("\\.", 2);
        if (parts.length != 2) {
            log.debug("Refresh token format invalid (expected 'session.secret'): {}", tokenInput);
            return Optional.empty();
        }

        UUID sessionId;

        try {
            sessionId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            log.debug("Invalid session UUID in refresh token: {}", parts[0]);
            return Optional.empty();
        }

        String secret = parts[1];
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findById(sessionId);
        if (tokenOpt.isEmpty()) {
            log.debug("Refresh token not found for sessionId: {}", sessionId);
            return Optional.empty();
        }

        RefreshToken token = tokenOpt.get();
        if (token.isRevoked()) {
            log.debug("Refresh token is revoked for sessionId: {}", sessionId);
            return Optional.empty();
        }

        OffsetDateTime expiresAt = OffsetDateTime.now();
        if (!token.getExpiresAt().isAfter(expiresAt)) {
            log.debug("Refresh token is expired. expiresAt={}, now={} sessionId={}", token.getExpiresAt(), expiresAt, sessionId);
            return Optional.empty();
        }

//        boolean match = tokenEncryptionService.matches(secret, token.getTokenHash());
//        log.debug("secret={}, Database Hash Token={}, sessionId={}, isMatch={}, Refresh token={}",
//                secret, token.getTokenHash(), sessionId, match, tokenInput);

        boolean legacyMatch = tokenEncryptionService.matches(tokenInput, token.getTokenHash());
        log.debug("secret={}, Database Hash Token={}, sessionId={}, isLegacyMatch={}, Refresh token={}",
                secret, token.getTokenHash(), sessionId, legacyMatch, tokenInput);

        if (!legacyMatch) {
            log.debug("Invalid refresh token. legacyMatch={}", legacyMatch);
            return Optional.empty();
        }

        return Optional.of(token);


        //return Optional.of(token);

/*        String secret = parts[1];
        Optional<RefreshToken> result = refreshTokenRepository.findById(sessionId)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(OffsetDateTime.now()))
                .filter(rt -> tokenEncryptionService.matches(secret, rt.getTokenHash()));
        log.trace("Refresh token validation result present: {}", result.isPresent());
        return result;*/

    }

    // rotate: mark old revoked and create new token
    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken, Duration ttl) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        String newToken = createRefreshToken(oldToken.getUser(), ttl);
        log.debug("Rotated refresh token for sessionId: {}", oldToken.getSessionId());
        return newToken;
    }

    //revoke session
    @Transactional
    public void revokeSessionById(UUID sessionId) {
        refreshTokenRepository.findById(sessionId)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                    log.debug("Revoked refresh sessionId: {}", sessionId);
                });
    }
}
