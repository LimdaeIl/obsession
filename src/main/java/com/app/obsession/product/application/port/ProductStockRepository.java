package com.app.obsession.product.application.port;

import com.app.obsession.product.domain.ProductStock;
import java.util.Optional;

public interface ProductStockRepository {

    ProductStock save(ProductStock stock);

    Optional<ProductStock> findByProductId(Long productId);

}
