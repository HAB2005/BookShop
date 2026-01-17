package com.example.system_backend.product.category.entity;

import com.example.system_backend.common.enums.CategoryStatus;
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
@ToString(exclude = { "parent", "children" }) // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "category")
public class Category {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "parent_id")
    private Integer parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CategoryStatus status = CategoryStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Self-referencing relationship for parent category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    // Children categories
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children;

    // ===== SIMPLE DOMAIN QUERIES =====
    /**
     * Domain query - check if category is active
     */
    public boolean isActive() {
        return this.status == CategoryStatus.ACTIVE;
    }

    /**
     * Domain query - check if category is root (has no parent)
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    // ===== SIMPLE DOMAIN MUTATIONS =====
    /**
     * Domain method to activate category (simple state change)
     */
    public void activate() {
        this.status = CategoryStatus.ACTIVE;
    }

    /**
     * Domain method to deactivate category (simple state change)
     * Note: Business validation should be done in CategoryValidationService
     */
    public void deactivate() {
        this.status = CategoryStatus.INACTIVE;
    }

    /**
     * Domain method to update name and slug (simple state change with basic validation)
     */
    public void updateNameAndSlug(String newName, String newSlug) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty", "NAME_EMPTY");
        }
        if (newSlug == null || newSlug.trim().isEmpty()) {
            throw new ValidationException("Category slug cannot be empty", "SLUG_EMPTY");
        }
        this.name = newName.trim();
        this.slug = newSlug.trim().toLowerCase();
    }
}
