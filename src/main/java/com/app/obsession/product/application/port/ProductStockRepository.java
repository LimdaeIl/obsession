package com.app.obsession.product.application.port;

import com.app.obsession.product.domain.ProductStock;

public interface ProductStockRepository {

    ProductStock save(ProductStock stock);

}
