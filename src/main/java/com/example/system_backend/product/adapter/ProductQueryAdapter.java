package com.example.system_backend.product.adapter;

import com.example.system_backend.common.port.ProductQueryPort;
import com.example.system_backend.product.application.service.ProductQueryService;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adapter that implements ProductQueryPort using product domain services
 */
@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {

    private final ProductQueryService productQueryService;

    @Override
    public Optional<BigDecimal> getProductPrice(Integer productId) {
        try {
            Product product = productQueryService.getProductById(productId);
            return Optional.of(product.getPrice());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getProductName(Integer productId) {
        try {
            Product product = productQueryService.getProductById(productId);
            return Optional.of(product.getName());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isProductAvailable(Integer productId) {
        try {
            Product product = productQueryService.getProductById(productId);
            return product.getStatus() == Product.Status.ACTIVE;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<ProductInfoPort> getProductInfo(Integer productId) {
        try {
            Product product = productQueryService.getProductById(productId);
            return Optional.of(new ProductInfoImpl(product));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Implementation of ProductInfoPort interface
     */
    private static class ProductInfoImpl implements ProductInfoPort {
        private final Product product;

        public ProductInfoImpl(Product product) {
            this.product = product;
        }

        @Override
        public Integer getProductId() {
            return product.getProductId();
        }

        @Override
        public String getName() {
            return product.getName();
        }

        @Override
        public BigDecimal getPrice() {
            return product.getPrice();
        }

        @Override
        public boolean isAvailable() {
            return product.getStatus() == Product.Status.ACTIVE;
        }
    }
}