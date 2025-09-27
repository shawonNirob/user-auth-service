package com.multillm.auth.repository;

import com.multillm.auth.model.AccountMember;
import com.multillm.auth.model.AccountMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountMemberRepository extends JpaRepository<AccountMember, AccountMemberId> {
    Optional<AccountMember> findByAccountIdAndUserId(Long accountId, Long userId);
}
