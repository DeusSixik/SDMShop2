package dev.sixik.sdmshop2.libs.shop.components.money;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.sdmeconomy.*;
import dev.sixik.sdmshop2.libs.shop.components.api.CostComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;
import java.util.Map;

public class MoneyCostComponent extends CostComponent {

    public static final IComponentType<MoneyCostComponent> TYPE = new Type();

    public static final ThreadLocal<DynamicStoredCurrency> DYNAMIC_CURRENCY = ThreadLocal.withInitial(() -> new DynamicStoredCurrency(EMPTY));

    @Getter
    private ResourceLocation moneyId;

    @Getter
    private double amount;

    public MoneyCostComponent() {
        this(EMPTY, 0);
    }

    public MoneyCostComponent(ResourceLocation moneyId, double amount) {
        this.moneyId = moneyId == null ? EMPTY : moneyId;
        this.amount = amount;
    }

    @Override
    public boolean canPay(Player player, double actualPrice) {
        final Map<ResourceLocation, IExternalCurrency> currencies = player.isLocalPlayer() ?
                SDMEconomyServiceClient.getAllCurrencies()
                : SDMEconomyCurrencyRegistry.getCurrenciesMap();

        if(currencies.containsKey(moneyId))
            /*
                Проверяем, хватает ли баланса на актуальную цену (actualPrice), а не на базовую (amount)
             */
            return currencies.get(moneyId).getBalance(player).doubleValue() >= actualPrice;

        final BankAccount account = player.isLocalPlayer()
                ? SDMEconomyServiceClient.getInstanceClient().getBankAccount()
                : SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());

        return account.getBalance(DYNAMIC_CURRENCY.get().setId(moneyId)).doubleValue() >= actualPrice;
    }

    @Override
    public void pay(Player player, double actualPrice) {
        if (player.isLocalPlayer()) {
            SDMShop2.LOGGER.warn("Call Pay methods on client!");
            return;
        }

        /*
            Мы конвертируем в BigDecimal именно actualPrice,
            Чтобы списать ровно ту сумму, которую насчитал процессор скидок.
         */
        final BigDecimal value = BigDecimal.valueOf(actualPrice);
        Map<ResourceLocation, IExternalCurrency> currencies = SDMEconomyCurrencyRegistry.getCurrenciesMap();

        if (currencies.containsKey(moneyId)) {
            currencies.get(moneyId).withdraw((ServerPlayer) player, value);
            return;
        }

        SDMEconomyService.getInstance()
                .getAccount(player.getGameProfile().getId())
                .modify(DYNAMIC_CURRENCY.get().setId(moneyId), value.negate());
    }

    @Override
    public double getBaseAmount() {
        return amount;
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

        @Override
        public MoneyCostComponent createDefault() {
            return new MoneyCostComponent();
        }

        @Override
        public MoneyCostComponent createFromBuilder(Object... args) {
            if(args.length != 2 && args.length != 3)
                throw new IllegalArgumentException("MoneyCostComponent.createFromBuilder() takes 2 or 3 arguments (String/ResourceLocation, double, (Optional) String)");

            Object obj = args[0];
            final var component = new MoneyCostComponent(obj instanceof ResourceLocation ? (ResourceLocation) obj : ResourceLocation.tryParse((String) obj), (double) args[1]);
            if(args.length == 3)
                component.setGroupId((String) args[2]);

            return component;
        }
    }
}
