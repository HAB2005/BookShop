package com.example.system_backend.user.repository;

import com.example.system_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin queries
    Page<User> findByRole(User.Role role, Pageable pageable);

    Page<User> findByStatus(User.Status status, Pageable pageable);

    Page<User> findByRoleAndStatus(User.Role role, User.Status status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE "
            + "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND "
            + "(:username IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :username, '%'))) AND "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(@Param("email") String email,
            @Param("username") String username,
            @Param("role") User.Role role,
            @Param("status") User.Status status,
            Pageable pageable);
}
