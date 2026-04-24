package dev.sixik.sdmshop2.libs.shop.components.money;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.sixik.sdmshop2.libs.sdmeconomy.IExternalCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyCurrencyRegistry;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.RewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import dev.sixik.sdmshop2.tests.economy.TestSDMCoin;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;
import java.util.Map;

public class MoneyRewardComponent extends RewardComponent {

    public static final IComponentType<MoneyRewardComponent> TYPE = new Type();

    @Getter
    private ResourceLocation moneyId;

    @Getter
    private double amount;

    public MoneyRewardComponent() {
        this(EMPTY, 0);
    }

    public MoneyRewardComponent(ResourceLocation moneyId, double amount) {
        this.moneyId = moneyId;
        this.amount = amount;
    }

    @Override
    public void reward(ServerPlayer player, int inAmount) {
        final BigDecimal value = BigDecimal.valueOf(amount * inAmount);
        Map<ResourceLocation, IExternalCurrency> currencies = SDMEconomyCurrencyRegistry.getCurrenciesMap();

        if (currencies.containsKey(moneyId)) {
            currencies.get(moneyId).deposit(player, value);
            return;
        }

        SDMEconomyService.getInstance()
                .getAccount(player.getGameProfile().getId())
                .modify(MoneyCostComponent.DYNAMIC_CURRENCY.get().setId(moneyId), value);
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<MoneyRewardComponent> {

        private static final ResourceLocation ID = new ResourceLocation("sdm", "reward_money");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(MoneyRewardComponent component) {
            JsonObject object = new JsonObject();
            object.addProperty("money_id", component.moneyId.toString());
            object.addProperty("amount", component.amount);
            return object;
        }

        @Override
        public MoneyRewardComponent deserialize(JsonObject json) {

            if(!json.has("money_id"))
                throw new JsonSyntaxException("Can't find 'money_id'!");

            if(!json.has("amount"))
                throw new JsonSyntaxException("Can't find 'amount'!");

            return new MoneyRewardComponent(
                    ResourceLocation.tryParse(json.get("money_id").getAsString()),
                    json.get("amount").getAsDouble()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MoneyRewardComponent component) {
            buf.writeResourceLocation(component.moneyId);
            buf.writeDouble(component.amount);
        }

        @Override
        public MoneyRewardComponent fromNetwork(FriendlyByteBuf buf) {
            return new MoneyRewardComponent(buf.readResourceLocation(), buf.readDouble());
        }

        @Override
        public MoneyRewardComponent createDefault() {
            return new MoneyRewardComponent();
        }

        @Override
        public MoneyRewardComponent createFromBuilder(Object... args) {
            if(args.length != 2)
                throw new IllegalArgumentException("MoneyRewardComponent.createFromBuilder() takes 2 arguments (String/ResourceLocation, double)");

            Object obj = args[0];
            return new MoneyRewardComponent(obj instanceof ResourceLocation ? (ResourceLocation) obj : ResourceLocation.tryParse((String) obj), (double) args[1]);
        }
    }
}
