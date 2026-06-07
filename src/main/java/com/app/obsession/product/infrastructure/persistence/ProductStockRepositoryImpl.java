package com.app.obsession.product.infrastructure.persistence;

import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final JpaProductStockRepository jpaProductStockRepository;

    @Override
    public ProductStock save(ProductStock productStock) {
        return jpaProductStockRepository.save(productStock);
    }
}
