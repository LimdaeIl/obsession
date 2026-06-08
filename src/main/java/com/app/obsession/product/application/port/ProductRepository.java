package com.app.obsession.product.application.port;

import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Page<Product> search(String keyword, ProductStatus status, Pageable pageable);

    Optional<Product> findById(Long productId);

    Optional<Product> findWithImagesById(Long productId);
}
