package com.example.system_backend.auth.repository;

import com.example.system_backend.auth.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

        Optional<AuthProvider> findByProviderAndProviderUserId(
                        AuthProvider.Provider provider, String providerUserId);

        List<AuthProvider> findByUserId(Integer userId);

        boolean existsByProviderAndProviderUserId(
                        AuthProvider.Provider provider, String providerUserId);

        @Query("SELECT ap FROM AuthProvider ap WHERE ap.userId = :userId AND ap.provider = :provider")
        Optional<AuthProvider> findByUserIdAndProvider(
                        @Param("userId") Integer userId,
                        @Param("provider") AuthProvider.Provider provider);

        void deleteByUserIdAndProvider(Integer userId, AuthProvider.Provider provider);
}