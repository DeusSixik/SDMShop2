package dev.sixik.sdmshop2.libs.shop.scripting.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.sixik.sdmshop2.libs.platform.ServerOperation;
import dev.sixik.sdmshop2.libs.shop.SDMShopPlatform;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.CostComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.RewardComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ShopScriptEvents {

    public static final Event<ScripConditionEvent> SCRIP_CONDITION_EVENT =
            EventFactory.of(listeners -> (player, component, scriptId) -> {
                if (listeners.isEmpty()) return false;

                boolean finalResult = true;
                for (ScripConditionEvent listener : listeners) {
                    if (!listener.invoke(player, component, scriptId)) {
                        return false;
                    }
                }
                return finalResult;
            });

    public static final Event<ScripRewardEvent> SCRIP_REWARD_EVENT =
            EventFactory.createLoop(new ScripRewardEvent[0]);

    public static final Event<ScriptCalculatePriceEvent> SCRIPT_CALCULATE_PRICE_EVENT =
            EventFactory.createLoop(new ScriptCalculatePriceEvent[0]);

    public static final Event<ScriptShopLoadEvent> SCRIPT_SHOP_LOAD_EVENT =
            EventFactory.createLoop(new ScriptShopLoadEvent[0]);

    @FunctionalInterface
    public interface ScripConditionEvent {

        boolean invoke(Player player, ConditionComponent component, String scriptId);
    }

    @FunctionalInterface
    public interface ScripRewardEvent {

        void invoke(Player player, int amount, RewardComponent component, String scriptId);
    }

    @FunctionalInterface
    public interface ScriptCalculatePriceEvent {

        void invoke(ShopOffer offer, MinecraftServer server, String chosenGroupId, Map<CostComponent, Double> prices);
    }

    @FunctionalInterface
    public interface ScriptShopLoadEvent {

        void invoke(MinecraftServer server, ShopTable table);
    }

    public static class Manager implements ServerOperation {

        @Override
        public void onReload() {
            ShopScriptEvents.SCRIP_CONDITION_EVENT.clearListeners();
            ShopScriptEvents.SCRIP_REWARD_EVENT.clearListeners();
            ShopScriptEvents.SCRIPT_CALCULATE_PRICE_EVENT.clearListeners();
            ShopScriptEvents.SCRIPT_SHOP_LOAD_EVENT.clearListeners();

            SDMShopPlatform.invokeKubeJSEvent.run();
        }
    }
}
