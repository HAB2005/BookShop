package com.example.system_backend.product.category.entity;

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
@ToString(exclude = {"parent", "children"}) // Tránh LazyInitializationException
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
    private Status status = Status.ACTIVE;

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

    public enum Status {
        ACTIVE, INACTIVE
    }

    // ===== DOMAIN METHODS =====

    /**
     * Domain method to check if category can have a parent
     */
    public boolean canHaveParent(Category parent) {
        if (parent == null) {
            return true; // Root category
        }
        if (parent.getCategoryId().equals(this.categoryId)) {
            return false; // Cannot be parent of itself
        }
        // Prevent circular reference - check if parent is descendant of this category
        return !parent.isDescendantOf(this);
    }

    /**
     * Domain method to check if category can be deactivated
     */
    public boolean canBeDeactivated() {
        // Business rule: Category can be deactivated if it has no active children
        if (children != null) {
            return children.stream().noneMatch(child -> child.getStatus() == Status.ACTIVE);
        }
        return true;
    }

    /**
     * Domain query - check if this category is descendant of another
     */
    public boolean isDescendantOf(Category other) {
        if (other == null || this.parent == null) {
            return false;
        }
        if (this.parent.getCategoryId().equals(other.getCategoryId())) {
            return true;
        }
        return this.parent.isDescendantOf(other);
    }

    /**
     * Domain query - get depth in hierarchy (root = 0)
     */
    public int getDepth() {
        if (this.parent == null) {
            return 0;
        }
        return 1 + this.parent.getDepth();
    }

    /**
     * Domain query - check if category can be deleted
     */
    public boolean canBeDeleted() {
        // Business rule: Only inactive categories without children can be deleted
        return this.status == Status.INACTIVE && 
               (children == null || children.isEmpty());
    }

    /**
     * Domain method to deactivate category
     */
    public void deactivate() {
        if (!canBeDeactivated()) {
            throw new ValidationException("Cannot deactivate category with active children", "CATEGORY_HAS_ACTIVE_CHILDREN");
        }
        this.status = Status.INACTIVE;
    }

    /**
     * Domain method to activate category
     */
    public void activate() {
        this.status = Status.ACTIVE;
    }

    /**
     * Domain query - check if category is active
     */
    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    /**
     * Domain query - check if category is root (has no parent)
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    /**
     * Domain method to update name and slug
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