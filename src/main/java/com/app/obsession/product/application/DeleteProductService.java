package com.app.obsession.product.application;

import com.app.obsession.product.application.command.DeleteProductCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductPermissionPolicy;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void delete(DeleteProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.ALREADY_DELETED_PRODUCT);
        }

        if (!ProductPermissionPolicy.canManage(product, command.actor())) {
            throw new ProductException(ProductErrorCode.PRODUCT_DELETE_FORBIDDEN);
        }

        product.delete();
    }
}
