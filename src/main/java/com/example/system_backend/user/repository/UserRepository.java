package com.example.system_backend.user.repository;

import com.example.system_backend.common.enums.UserRole;
import com.example.system_backend.common.enums.UserStatus;
import com.example.system_backend.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin queries
    Page<Role> findByRole(UserRole role, Pageable pageable);

    Page<Role> findByStatus(UserStatus status, Pageable pageable);

    Page<Role> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE "
            + "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND "
            + "(:username IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :username, '%'))) AND "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:status IS NULL OR u.status = :status)")
    Page<Role> findUsersWithFilters(@Param("email") String email,
            @Param("username") String username,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable);
}
