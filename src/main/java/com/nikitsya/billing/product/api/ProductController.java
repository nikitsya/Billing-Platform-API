package com.nikitsya.billing.product.api;

import com.nikitsya.billing.product.model.Product;
import com.nikitsya.billing.product.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product save(@Valid @RequestBody Product product) {
        product.setActive(true);
        return productRepository.save(product);
    }
}
