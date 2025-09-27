package com.multillm.auth.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class TokenEncryptionService {
    private static final BCryptPasswordEncoder B_CRYPT = new BCryptPasswordEncoder();

    // Use BCrypt for new tokens for stronger security and to match legacy data
    public String hash(String raw) {
        return B_CRYPT.encode(raw);
    }

    // Backward-compatible matcher: supports both BCrypt (legacy/current) and SHA-256 hex (older build)
    public boolean matches(String raw, String hashed) {
        if (hashed == null) {
            return false;
        }

        if (isBcryptHash(hashed)) {
            return B_CRYPT.matches(raw, hashed);
        }

        // Fallback to SHA-256 hex comparison
        return sha256(raw).equalsIgnoreCase(hashed);
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new IllegalStateException("Hashing unavailable");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
