package com.multillm.auth.repository;

import com.multillm.auth.model.RefreshToken;
import com.multillm.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash (String tokenHash); //will not work due to salt at BCrypt
    void deleteByUser (User user);
    List<RefreshToken> findAllByUser(User user);
    List<RefreshToken> findAllByUserAndRevokedFalseAndExpiresAtAfter (User user, LocalDateTime expiresAt);
}
