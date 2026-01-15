package com.example.system_backend.user.adapter;

import com.example.system_backend.common.port.UserPort;
import com.example.system_backend.common.port.UserQueryPort;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that implements UserQueryPort for auth module to use
 * This allows auth module to query users without depending on user module directly
 */
@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserPort> findById(Integer userId) {
        return userRepository.findById(userId).map(user -> (UserPort) user);
    }

    @Override
    public Optional<UserPort> findByEmail(String email) {
        return userRepository.findByEmail(email).map(user -> (UserPort) user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
