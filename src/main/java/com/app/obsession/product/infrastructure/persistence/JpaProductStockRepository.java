package com.app.obsession.product.infrastructure.persistence;

import com.app.obsession.product.domain.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductStockRepository extends JpaRepository<ProductStock, Long> {
}
