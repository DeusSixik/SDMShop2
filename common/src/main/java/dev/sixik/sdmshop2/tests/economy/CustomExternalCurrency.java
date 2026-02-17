package dev.sixik.sdmshop2.tests.economy;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.sdmeconomy.ICurrencyType;
import dev.sixik.sdmshop2.libs.sdmeconomy.IExternalCurrency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigDecimal;

public class CustomExternalCurrency implements IExternalCurrency {

    private final ResourceLocation id;
    private final Component displayName;
    private final Item itemType;

    public CustomExternalCurrency(ResourceLocation id, Item itemType) {
        this.id = id;
        this.displayName = Component.translatable(id.toString().replace(":", "_"));
        this.itemType = itemType;
    }

    @Override
    public boolean withdraw(ServerPlayer player, BigDecimal amount, boolean simulate) {
        BigDecimal currentBalance = getBalance(player);
        if (currentBalance.doubleValue() < amount.doubleValue()) return false;

        if (!simulate) {
            // Удаляем предметы. В ваниле есть helper, но надежнее ручной обход,
            // чтобы точно контролировать NBT если нужно.
            int remaining = amount.intValue();
            Inventory inv = player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() == itemType) {
                    int extract = Math.min(stack.getCount(), remaining);
                    stack.shrink(extract);
                    remaining -= extract;
                    if (remaining <= 0) break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean deposit(ServerPlayer player, BigDecimal amount, boolean simulate) {
        // Проверка места в инвентаре сложнее, поэтому упростим:
        // Всегда true, а если нет места — дропаем.
        if (!simulate) {
            ItemStack stack = new ItemStack(itemType, amount.intValue());
            // giveToPlayer вернет false, если инвентарь полон
            boolean added = player.getInventory().add(stack);

            if (!added) {
                // Если не влезло — кидаем под ноги, чтобы игрок не потерял деньги
                player.drop(stack, false);
            }
        }
        return true;
    }

    @Override
    public BigDecimal getBalance(Player player) {
        return new BigDecimal(player.getInventory().countItem(itemType));
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public String format(BigDecimal decimal) {
        return decimal.toString();
    }

    @Override
    public int getColor() {
        return 0;
    }

    public static class CustomExternalCurrencyType implements ICurrencyType<CustomExternalCurrency> {

        @Override
        public CustomExternalCurrency deserialize(ResourceLocation id, JsonObject json) {

            String itemIdStr = json.get("item").getAsString();
            ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);

            Item item = BuiltInRegistries.ITEM.get(itemId);

            if (item == null || item == Items.AIR) {
                throw new IllegalArgumentException("Item not found: " + itemIdStr);
            }

            return new CustomExternalCurrency(id, item);
        }

        @Override
        public JsonObject serialize(CustomExternalCurrency currency) {
            JsonObject json = new JsonObject();
            serializeType(json, "item");
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(currency.itemType).toString());
            return json;
        }
    }
}
