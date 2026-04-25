package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public abstract class PromoComponent extends ShopComponent {

    @Getter
    @Setter
    @ComponentConfig(translationKey = "shop.component.promo.conditions.promo_id")
    private String promoId = "";

    /**
     * Главный метод проверки. Возвращает true, если акция активна прямо сейчас.
     */
    public boolean isActive(MinecraftServer server) {
        return true;
    }

    public boolean isActive(Player player) {
        return isActive(player.getServer());
    }

    @Override
    public void additionalSerialize(JsonObject json) {
        if(promoId != null && !promoId.isEmpty())
            json.addProperty("promo_id", promoId);
    }

    @Override
    public void additionalDeserialize(JsonObject json) {
        if(json.has("promo_id"))
            promoId = json.get("promo_id").getAsString();
    }

    @Override
    public void additionalFromNetwork(FriendlyByteBuf buf) {
        promoId = buf.readUtf();
    }

    @Override
    public void additionalToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(promoId);
    }
}
