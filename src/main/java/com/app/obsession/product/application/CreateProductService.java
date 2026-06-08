package com.app.obsession.product.application;

import com.app.obsession.product.application.command.CreateProductCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.application.result.RegisterProductResult;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductActor;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public RegisterProductResult create(CreateProductCommand command) {
        validate(command);

        ProductActor actor = command.actor();

        Product product = Product.create(
                actor.memberId(),
                command.name(),
                command.description(),
                command.price(),
                command.status()
        );

        if (command.imageUrls() != null) {
            for (int i = 0; i < command.imageUrls().size(); i++) {
                product.addImage(command.imageUrls().get(i), i + 1);
            }
        }

        Product savedProduct = productRepository.save(product);

        ProductStock stock = ProductStock.create(
                savedProduct.getId(),
                command.initialStock()
        );

        productStockRepository.save(stock);

        return new RegisterProductResult(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getStatus(),
                command.initialStock()
        );
    }

    private void validate(CreateProductCommand command) {
        if (command.actor() == null || !command.actor().canCreateProduct()) {
            throw new ProductException(ProductErrorCode.PRODUCT_CREATE_FORBIDDEN);
        }

        if (command.status() == ProductStatus.ON_SALE
                && command.initialStock() <= 0) {
            throw new ProductException(ProductErrorCode.ON_SALE_PRODUCT_REQUIRES_STOCK);
        }
    }
}
