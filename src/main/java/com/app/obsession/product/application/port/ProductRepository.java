package com.app.obsession.product.application.port;

import com.app.obsession.product.domain.Product;

public interface ProductRepository {

    Product save(Product product);
}
