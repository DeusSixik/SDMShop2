package dev.sixik.sdmshop2.libs.shop.components;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.sixik.sdmshop2.libs.sdmeconomy.BankAccount;
import dev.sixik.sdmshop2.libs.sdmeconomy.DynamicStoredCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.CostComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;

public class MoneyCostComponent extends CostComponent {

    public static final IComponentType<MoneyCostComponent> TYPE = new Type();

    private final ResourceLocation moneyId;
    private final double amount;

    public MoneyCostComponent(ResourceLocation moneyId, double amount) {
        this.moneyId = moneyId;
        this.amount = amount;
    }

    @Override
    public boolean canPay(Player player) {
        final BankAccount account = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());
        return account.getBalance(new DynamicStoredCurrency(moneyId)).doubleValue() >= amount;
    }

    @Override
    public void pay(Player player) {
        final BankAccount account = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());
        account.modify(new DynamicStoredCurrency(moneyId), new BigDecimal(-amount));
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<MoneyCostComponent> {

        private static final ResourceLocation ID = new ResourceLocation("sdm", "cost_money");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public MoneyCostComponent deserialize(JsonObject json) {

            if(!json.has("money_id"))
                throw new JsonSyntaxException("Can't find 'money_id'!");

            if(!json.has("amount"))
                throw new JsonSyntaxException("Can't find 'amount'!");

            return new MoneyCostComponent(
                    ResourceLocation.tryParse(json.get("money_id").getAsString()),
                    json.get("amount").getAsDouble()
            );
        }

        @Override
        public JsonObject serialize(MoneyCostComponent component) {
            JsonObject object = new JsonObject();
            object.addProperty("money_id", component.moneyId.toString());
            object.addProperty("amount", component.amount);
            return object;
        }

        @Override
        public MoneyCostComponent fromNetwork(FriendlyByteBuf buf) {
            return new MoneyCostComponent(buf.readResourceLocation(), buf.readDouble());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MoneyCostComponent component) {
            buf.writeResourceLocation(component.moneyId);
            buf.writeDouble(component.amount);
        }
    }
}
