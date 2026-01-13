package com.example.system_backend.product.entity;

import com.example.system_backend.product.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"product", "category"}) // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "product_category")
@IdClass(ProductCategoryId.class)
public class ProductCategory {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "product_id")
    private Integer productId;

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "category_id")
    private Integer categoryId;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}
