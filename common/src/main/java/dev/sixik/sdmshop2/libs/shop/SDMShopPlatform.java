package dev.sixik.sdmshop2.libs.shop;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class SDMShopPlatform {

    public static Runnable invokeKubeJSEvent = () -> {};

    @Environment(EnvType.CLIENT)
    public static ShopInstance getClientShopInstance() {
        return SDMShopClient.Shop;
    }

    public static Optional<ShopInstance> getServerShopInstance(ResourceLocation shopId) {
        Optional<ShopTable> opt = getServerShopData();
        if(opt.isEmpty()) return Optional.empty();

        final ShopTable manager = opt.get();
        return Optional.ofNullable(manager.getShop(shopId));
    }

    public static Optional<ShopTable> getServerShopData() {
        return Optional.ofNullable(ShopTable.Instance);
    }
}
