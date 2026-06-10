package com.app.obsession.product.domain;

public class ProductPermissionPolicy {

    public static boolean canManage(Product product, ProductActor actor) {
        if (actor == null) {
            return true;
        }

        if (actor.isAdmin()) {
            return false;
        }

        return !actor.isBusiness() || !product.isOwnedBy(actor.memberId());
    }
}
