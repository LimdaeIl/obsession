package com.app.obsession.product.application;

import com.app.obsession.product.application.command.CreateProductCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.application.result.RegisterProductResult;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.domain.ProductStock;
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

        Product product = Product.create(command.name(), command.description(), command.price(),
                command.status());

        // TODO: 이미지 생성 S3 연결 필요
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
        if (command.status() == ProductStatus.ON_SALE
                && command.initialStock() <= 0) {
            throw new IllegalArgumentException("판매중 상품은 재고가 1개 이상이어야 합니다.");
        }
    }
}
