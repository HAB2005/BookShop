package com.example.system_backend.otp.repository;

import com.example.system_backend.otp.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByPhoneAndCodeAndVerifiedFalseAndExpiredAtAfter(
            String phone, String code, LocalDateTime currentTime);

    Optional<Otp> findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);

    @Modifying
    @Query("UPDATE Otp o SET o.verified = true WHERE o.phone = :phone AND o.code = :code AND o.verified = false")
    int markAsVerified(@Param("phone") String phone, @Param("code") String code);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiredAt < :currentTime")
    int deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(o) FROM Otp o WHERE o.phone = :phone AND o.createdAt > :since")
    long countByPhoneAndCreatedAtAfter(@Param("phone") String phone, @Param("since") LocalDateTime since);

    boolean existsByPhoneAndCodeAndVerifiedFalseAndExpiredAtAfter(
            String phone, String code, LocalDateTime currentTime);
}