package com.example.system_backend.common.security;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // In-memory blacklist (trong production nên dùng Redis)
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        // Kiểm tra token có trong blacklist không
        if (blacklistedTokens.contains(token)) {
            return true;
        }

        // Tự động xóa token đã hết hạn khỏi blacklist để tiết kiệm memory
        cleanupExpiredTokens();

        return false;
    }

    private void cleanupExpiredTokens() {
        // Xóa các token đã hết hạn khỏi blacklist
        blacklistedTokens.removeIf(token -> {
            try {
                Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());
                return expiration.before(new Date());
            } catch (Exception e) {
                // Nếu token không valid thì cũng xóa khỏi blacklist
                return true;
            }
        });
    }

    // Method để admin có thể xem số lượng token trong blacklist
    public int getBlacklistSize() {
        cleanupExpiredTokens();
        return blacklistedTokens.size();
    }
}