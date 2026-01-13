package com.example.system_backend.product.book.entity;

import com.example.system_backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product") // Tránh LazyInitializationException
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "book")
public class Book {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "product_id", nullable = false, unique = true)
    private Integer productId;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "language", length = 50)
    private String language;

    // Relationship with Product
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}