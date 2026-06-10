package com.app.obsession.product.application;

import com.app.obsession.product.application.command.UpdateProductCommand;
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
public class UpdateProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void update(UpdateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.DELETED_PRODUCT_CANNOT_BE_UPDATED);
        }

        if (ProductPermissionPolicy.canManage(product, command.actor())) {
            throw new ProductException(ProductErrorCode.PRODUCT_UPDATE_FORBIDDEN);
        }

        product.changeName(command.name());
        product.changeDescription(command.description());
        product.changePrice(command.price());
        product.changeStatus(command.status());
    }
}
