package com.app.obsession.product.infrastructure.persistence;

import com.app.obsession.product.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {


    @EntityGraph(attributePaths = "images")
    Optional<Product> findWithImagesById(Long id);

}
