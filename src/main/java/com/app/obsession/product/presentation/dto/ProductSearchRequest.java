package com.app.obsession.product.presentation.dto;

import com.app.obsession.product.domain.ProductStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ProductSearchRequest(
        String keyword,
        ProductStatus status,
        Integer page,
        Integer size
) {

    public Pageable toPageable() {
        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size < 0 ? 20 : size;

        return PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

}
