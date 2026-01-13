package com.example.system_backend.product.image.entity;

import com.example.system_backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product") // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "product_image")
public class ProductImage {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPrimary = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationship with Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // ===== SIMPLE SETTERS FOR BUSINESS OPERATIONS =====
    /**
     * Set as primary image
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Unset as primary image
     */
    public void unsetAsPrimary() {
        this.isPrimary = false;
    }

    /**
     * Update sort order (with basic validation)
     */
    public void updateSortOrder(Integer newSortOrder) {
        if (newSortOrder == null || newSortOrder < 0) {
            throw new IllegalArgumentException("Sort order must be non-negative");
        }
        this.sortOrder = newSortOrder;
    }

    /**
     * Update image URL (with basic validation)
     */
    public void updateImageUrl(String newImageUrl) {
        if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        this.imageUrl = newImageUrl.trim();
    }

    /**
     * Simple check if image is primary
     */
    public boolean isPrimary() {
        return this.isPrimary != null && this.isPrimary;
    }
}
