package com.app.obsession.product.application;

import com.app.obsession.product.application.command.UpdateProductCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductPermissionPolicy;
import com.app.obsession.product.domain.ProductStatus;
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
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 수정할 수 없습니다.");
        }

        if (!ProductPermissionPolicy.canManage(product, command.actor())) {
            throw new IllegalStateException("상품을 수정할 권한이 없습니다.");
        }

        product.changeName(command.name());
        product.changeDescription(command.description());
        product.changePrice(command.price());
        product.changeStatus(command.status());
    }
}

