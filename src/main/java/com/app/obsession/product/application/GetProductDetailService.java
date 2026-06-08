package com.app.obsession.product.application;

import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetProductDetailService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        return product;
    }
}
