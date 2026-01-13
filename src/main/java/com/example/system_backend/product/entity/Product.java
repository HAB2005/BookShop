package com.example.system_backend.product.entity;

import com.example.system_backend.product.book.entity.Book;
import com.example.system_backend.product.image.entity.ProductImage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"productCategories", "book", "images"}) // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "product")
public class Product {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    // Many-to-Many relationship with Category through ProductCategory
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductCategory> productCategories;

    // One-to-One relationship with Book
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private Book book;

    // One-to-Many relationship with ProductImage
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    public enum Status {
        ACTIVE, INACTIVE, DELETED
    }
}
