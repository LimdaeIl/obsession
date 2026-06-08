package com.app.obsession.product.domain;

public record ProductActor(
        Long memberId,
        String role
) {

    public boolean isAdmin() {
        return "ADMIN".equals(normalizedRole());
    }

    public boolean isBusiness() {
        return "BUSINESS".equals(normalizedRole());
    }

    public boolean canCreateProduct() {
        return isBusiness() || isAdmin();
    }

    private String normalizedRole() {
        if (role == null || role.isBlank()) {
            return "";
        }

        return role
                .trim()
                .toUpperCase()
                .replaceFirst("^ROLE_", "");
    }
}
