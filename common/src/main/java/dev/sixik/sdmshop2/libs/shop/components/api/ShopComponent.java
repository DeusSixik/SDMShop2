package dev.sixik.sdmshop2.libs.shop.components.api;

import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.components.api.exceptions.ValidationException;

public abstract class ShopComponent {

    private ShopEntity root;

    public void init() { }

    public int priority() {
        return 0;
    }

    public abstract IComponentType<?> getType();

    public final ShopEntity getRoot() {
        return root;
    }

    public final <T extends ShopEntity> T getRoots() {
        return (T) root;
    }

    public final void setRoot(ShopEntity entity) {
        if(root != null) return;
        this.root = entity;
    }

    public boolean shouldSync() {
        return true;
    }

    public void validate() throws ValidationException {}
}
