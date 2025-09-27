package com.multillm.auth.repository;

import com.multillm.auth.model.AuthProvider;
import com.multillm.auth.model.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider,Long> {
    Optional<AuthProvider> findByProviderAndProviderUid(Provider provider, String providerUid);
}
