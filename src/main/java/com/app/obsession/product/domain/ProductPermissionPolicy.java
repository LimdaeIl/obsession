package com.app.obsession.product.domain;

public class ProductPermissionPolicy {

    public static boolean canManage(Product product, ProductActor actor) {
        if (actor.isAdmin()) {
            return true;
        }

        if (actor.isBusiness() && product.isOwnedBy(actor.memberId())) {
            return true;
        }

        return false;
    }
}
