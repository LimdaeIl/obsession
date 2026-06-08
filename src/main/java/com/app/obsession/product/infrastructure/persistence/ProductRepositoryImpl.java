package com.app.obsession.product.infrastructure.persistence;

import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    @Override
    public Product save(Product product) {
        return jpaProductRepository.save(product);
    }

    @Override
    public Page<Product> search(String keyword, ProductStatus status, Pageable pageable) {
        return jpaProductRepository.findAll(toSpecification(keyword, status), pageable);
    }

    private Specification<Product> toSpecification(String keyword, ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword + "%";

                predicates.add(
                        cb.or(
                                cb.like(root.get("name"), likeKeyword),
                                cb.like(root.get("description"), likeKeyword)
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return jpaProductRepository.findById(productId);
    }

    @Override
    public Optional<Product> findWithImagesById(Long productId) {
        return jpaProductRepository.findWithImagesById(productId);
    }
}
