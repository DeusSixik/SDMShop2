package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Абстрактный компонент, представляющий стоимость покупки.
 * Отвечает за проверку наличия средств и процесс оплаты.
 */
public abstract class CostComponent extends ShopComponent {

    /**
     * Уникальный ID для группировки нескольких валют
     */
    @Getter
    @Setter
    private String groupId = "";

    /**
     * Проверяет, может ли игрок оплатить данную стоимость.
     *
     * @param player Игрок для проверки
     * @return true, если средств достаточно, иначе false
     */
    public abstract boolean canPay(Player player, double actualPrice);

    public final void payInternal(Player player, double actualPrice) {
        if(canPay(player, actualPrice))
            pay(player, actualPrice);
    }

    /**
     * Списывает стоимость у игрока.
     *
     * @param player Игрок, с которого списываются средства
     */
    public abstract void pay(Player player, double actualPrice);

    /**
     * Возвращает базовую стоимость до применения скидок.
     * Если компонент не поддерживает числовые скидки (например, квестовый предмет),
     * можно возвращать 0 или игнорировать его в скидках.
     */
    public abstract double getBaseAmount();

    @Override
    public void additionalSerialize(JsonObject json) {
        if(groupId != null && !groupId.isEmpty())
            json.addProperty("group_id", groupId);
    }

    @Override
    public void additionalDeserialize(JsonObject json) {
        if(json.has("group_id"))
            groupId = json.get("group_id").getAsString();
    }

    @Override
    public void additionalToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(groupId);
    }

    @Override
    public void additionalFromNetwork(FriendlyByteBuf buf) {
        groupId = buf.readUtf();
    }
}
