package com.app.obsession.product.application;

import com.app.obsession.product.application.command.DeleteProductCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductPermissionPolicy;
import com.app.obsession.product.domain.ProductStatus;
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
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 상품입니다.");
        }

        if (!ProductPermissionPolicy.canManage(product, command.actor())) {
            throw new IllegalStateException("상품을 삭제할 권한이 없습니다.");
        }

        product.delete();
    }
}
