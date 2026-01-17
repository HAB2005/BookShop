package com.example.system_backend.user.adapter;

import com.example.system_backend.common.enums.UserRole;
import com.example.system_backend.common.enums.UserStatus;
import com.example.system_backend.common.port.UserCommandPort;
import com.example.system_backend.common.port.UserPort;
import com.example.system_backend.user.domain.UserValidationService;
import com.example.system_backend.user.entity.Role;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements UserCommandPort for auth module to use
 * This allows auth module to create users without depending on user module
 * directly
 */
@Component
@RequiredArgsConstructor
public class UserCommandAdapter implements UserCommandPort {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;

    @Override
    public UserPort createUserWithEmail(String email, String fullName) {
        Role user = new Role();
        userValidationService.changeUserEmail(user, email);
        user.setFullName(fullName);
        user.setRole(UserRole.customer);
        user.setStatus(UserStatus.active);
        return userRepository.save(user);
    }

    @Override
    public UserPort createUserWithoutEmail() {
        Role user = new Role();
        user.setRole(UserRole.customer);
        user.setStatus(UserStatus.active);
        return userRepository.save(user);
    }

    @Override
    public UserPort updateUserProfile(UserPort userPort, String email, String fullName) {
        if (!(userPort instanceof Role)) {
            throw new IllegalArgumentException("UserPort must be User entity");
        }
        Role user = (Role) userPort;
        userValidationService.updateUserProfile(user, email, fullName);
        return userRepository.save(user);
    }

    @Override
    public UserPort saveUser(UserPort userPort) {
        if (!(userPort instanceof Role)) {
            throw new IllegalArgumentException("UserPort must be User entity");
        }
        return userRepository.save((Role) userPort);
    }
}
