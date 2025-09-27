package com.multillm.auth.util;

import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptoUtils {
    public static String generateSecureRandomUrlSafeString(int byteLength){
        log.trace("Generating secure random URL-safe string of {} bytes", byteLength);
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
