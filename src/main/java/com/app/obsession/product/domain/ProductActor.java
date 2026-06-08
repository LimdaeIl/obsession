package com.app.obsession.product.domain;

public record ProductActor(
        Long memberId,
        String role
) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isBusiness() {
        return "BUSINESS".equals(role);
    }

    public boolean canCreateProduct() {
        return isBusiness() || isAdmin();
    }
}
