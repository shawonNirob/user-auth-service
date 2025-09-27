package com.multillm.auth.repository;

import com.multillm.auth.model.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation,Long> {
    Optional<TeamInvitation> findByTokenAndAcceptedIsFalseAndExpiresAtAfter(String token, OffsetDateTime now);
    List<TeamInvitation> findByEmailAndAcceptedIsFalseAndExpiresAtAfter(String email, OffsetDateTime now);

    Optional<TeamInvitation> findByToken(String token);

    Optional<TeamInvitation> findByAccountId(Long accountId);
}
