package com.nikitsya.billing.product.repository;

import com.nikitsya.billing.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
