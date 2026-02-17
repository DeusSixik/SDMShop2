package dev.sixik.sdmshop2.libs.sdmeconomy.custom_currency;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.sixik.sdmshop2.libs.sdmeconomy.ICurrencyType;
import dev.sixik.sdmshop2.libs.sdmeconomy.IExternalCurrency;
import dev.sixik.sdmshop2.utils.ShopItemHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigDecimal;

public class ExternalItemCurrency implements IExternalCurrency {

    private final ResourceLocation id;
    private final Component displayName;
    private final ItemStack itemType;

    public ExternalItemCurrency(ResourceLocation id, ItemStack itemType) {
        this.id = id;
        this.displayName = Component.translatable(id.toString().replace(":", "_"));
        this.itemType = itemType;
    }

    @Override
    public boolean withdraw(ServerPlayer player, BigDecimal amount, boolean simulate) {
        BigDecimal currentBalance = getBalance(player);
        if (currentBalance.doubleValue() < amount.doubleValue()) return false;

        if (!simulate) {
            return ShopItemHelper.shrinkItem(player.getInventory(), itemType, amount.intValue(), true, false);
        }
        return true;
    }

    @Override
    public boolean deposit(ServerPlayer player, BigDecimal amount, boolean simulate) {
        if (!simulate) {
            ItemStack stack = itemType.copyWithCount(amount.intValue());
            return ShopItemHelper.giveItems(player, stack, amount.intValue());
        }
        return true;
    }

    @Override
    public BigDecimal getBalance(Player player) {
        return new BigDecimal(ShopItemHelper.countItem(player.getInventory(), itemType, true, false));
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

    public static class ExternalItemCurrencyType implements ICurrencyType<ExternalItemCurrency> {

        @Override
        public ExternalItemCurrency deserialize(ResourceLocation id, JsonObject json) {

            if(!json.has("item"))
                throw new NullPointerException("Param with id 'item' not exists!");

            String itemIdStr = json.get("item").getAsString();
            ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);

            Item item = BuiltInRegistries.ITEM.get(itemId);

            if (item == null || item == Items.AIR) {
                throw new IllegalArgumentException("Item not found: " + itemIdStr);
            }

            CompoundTag nbt = null;
            if (json.has("nbt")) {
                String nbtString = json.get("nbt").getAsString();
                try {
                    nbt = TagParser.parseTag(nbtString);
                } catch (Exception e) {
                    throw new JsonSyntaxException("Invalid NBT in currency " + id + ": " + e.getMessage());
                }
            }


            ItemStack itemStack = item.getDefaultInstance();

            if(nbt != null)
                itemStack.setTag(nbt);

            return new ExternalItemCurrency(id, itemStack);
        }

        @Override
        public JsonObject serialize(ExternalItemCurrency currency) {
            final ItemStack item = currency.itemType;

            JsonObject json = new JsonObject();
            serializeType(json, "item");
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());

            if(item.getTag() != null) {
                json.addProperty("nbt", item.getTag().toString());
            }

            return json;
        }
    }
}
