package com.example.system_backend.common.port;

import java.util.Optional;

/**
 * UserQueryPort interface - allows auth module to query users without depending
 * on UserRepository
 * This breaks the circular dependency between auth and user modules
 */
public interface UserQueryPort {
    Optional<UserPort> findById(Integer userId);

    Optional<UserPort> findByEmail(String email);

    boolean existsByEmail(String email);
}
