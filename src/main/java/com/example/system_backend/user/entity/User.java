package com.example.system_backend.user.entity;

import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.common.exception.ValidationException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "authProviders") // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Chỉ dùng explicit fields
@Table(name = "users")
public class User {

    @EqualsAndHashCode.Include // Chỉ dùng ID cho equals/hashCode
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.active;

    // Relationship with AuthProvider
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AuthProvider> authProviders;

    public enum Role {
        customer, admin;

        /**
         * Parse role string with validation
         */
        public static Role parseRole(String roleStr) {
            try {
                return Role.valueOf(roleStr.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid role. Must be 'customer' or 'admin'", "INVALID_ROLE");
            }
        }
    }

    public enum Status {
        active, inactive, banned;

        /**
         * Parse status string with validation
         */
        public static Status parseStatus(String statusStr) {
            try {
                return Status.valueOf(statusStr.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid status. Must be 'active', 'inactive', or 'banned'",
                        "INVALID_STATUS");
            }
        }
    }
}
