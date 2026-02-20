package dev.sixik.sdmshop2.libs.shop.components.api;

import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.components.api.exceptions.ValidationException;
import net.minecraft.resources.ResourceLocation;

public abstract class ShopComponent {

    public static ResourceLocation EMPTY = ResourceLocation.tryBuild("sdm", "null");


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

    @Deprecated
    public void validate() throws ValidationException {}

    public static boolean isEmpty(ResourceLocation id) {
        return EMPTY.equals(id);
    }
}
