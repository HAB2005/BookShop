package com.example.system_backend.otp.service;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.otp.entity.Otp;
import com.example.system_backend.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS_PER_HOUR = 5;

    @Transactional
    public String generateAndSendOtp(String phone) {
        // Kiểm tra rate limiting
        checkRateLimit(phone);
        
        // Generate OTP code
        String otpCode = generateOtpCode();
        
        // Tạo OTP entity
        Otp otp = new Otp();
        otp.setPhone(phone);
        otp.setCode(otpCode);
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otp.setVerified(false);
        
        otpRepository.save(otp);
        
        // TODO: Tích hợp SMS service để gửi OTP
        sendOtpViaSms(phone, otpCode);
        
        log.info("OTP generated for phone: {}", phone);
        return otpCode; // Trong production không nên return OTP code
    }

    @Transactional
    public boolean verifyOtp(String phone, String code) {
        Optional<Otp> otpOpt = otpRepository.findByPhoneAndCodeAndVerifiedFalseAndExpiredAtAfter(
                phone, code, LocalDateTime.now());
        
        if (otpOpt.isEmpty()) {
            throw new ValidationException("Invalid or expired OTP code", "INVALID_OTP");
        }
        
        // Mark as verified
        int updated = otpRepository.markAsVerified(phone, code);
        
        if (updated > 0) {
            log.info("OTP verified successfully for phone: {}", phone);
            return true;
        }
        
        return false;
    }

    public boolean isOtpValid(String phone, String code) {
        return otpRepository.existsByPhoneAndCodeAndVerifiedFalseAndExpiredAtAfter(
                phone, code, LocalDateTime.now());
    }

    @Transactional
    public void cleanupExpiredOtps() {
        int deleted = otpRepository.deleteExpiredOtps(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired OTP records", deleted);
        }
    }

    private void checkRateLimit(String phone) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = otpRepository.countByPhoneAndCreatedAtAfter(phone, oneHourAgo);
        
        if (recentAttempts >= MAX_OTP_ATTEMPTS_PER_HOUR) {
            throw new ValidationException(
                "Too many OTP requests. Please try again later.", 
                "OTP_RATE_LIMIT_EXCEEDED"
            );
        }
    }

    private String generateOtpCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    private void sendOtpViaSms(String phone, String otpCode) {
        // TODO: Implement SMS service integration
        // For now, just log the OTP (remove in production)
        log.info("SMS OTP for {}: {}", phone, otpCode);
        
        // Example integration points:
        // - Twilio SMS API
        // - AWS SNS
        // - Firebase Cloud Messaging
        // - Local SMS gateway
    }
}