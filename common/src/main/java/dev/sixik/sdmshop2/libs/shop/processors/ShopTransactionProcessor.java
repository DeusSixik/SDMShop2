package dev.sixik.sdmshop2.libs.shop.processors;

import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.components.api.*;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.libs.shop.events.ShopServerEvents;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class ShopTransactionProcessor {

    /**
     * Выполняет транзакцию покупки.
     * * @param offer Товар, который покупают.
     * @param player Игрок-покупатель.
     * @param chosenGroupId Выбранный способ оплаты (ID группы).
     * @param amount Количество покупаемого товара (например, 1, 5, 10).
     * @return true, если покупка успешна, иначе false.
     */
    public static boolean executePlayerPurchase(
            final ShopOffer offer,
            final ServerPlayer player,
            final String chosenGroupId,
            final int amount
    ) {
        if (offer == null || player == null || amount <= 0) return false;
        final MinecraftServer server = player.getServer();

        // ФАЗА 1: ПРОВЕРКА УСЛОВИЙ (Лимиты, Доступ, Время)

        /*
            Проверяем обычные условия (права, квесты и т.д.)
         */
        for (ConditionComponent condition : offer.getComponents(ConditionComponent.class)) {
            if (!condition.isChecked(player)) return false;
        }

        /*
            Проверяем лимиты с учетом желаемого количества (amount)
         */
        List<LimiterComponent> limiters = offer.getComponents(LimiterComponent.class);
        for (LimiterComponent limiter : limiters) {
            if (!limiter.isChecked(player, amount)) return false;
        }

        // ФАЗА 2: РАСЧЕТ И ПРОВЕРКА СТОИМОСТИ

        /*
             Получаем мапу: Компонент -> Финальная цена со скидками
         */
        Map<CostComponent, Double> finalCosts = calculateFinalCosts(offer, server, chosenGroupId);

        /*
            Если стоимости нет, но товар не бесплатный (компоненты цены есть),
            значит игрок прислал левый chosenGroupId. Блокируем.
         */
        if (finalCosts.isEmpty() && !offer.getComponents(CostComponent.class).isEmpty()) {
            return false;
        }

        /*
            Проверяем, хватает ли денег на всё
         */
        for (Map.Entry<CostComponent, Double> entry : finalCosts.entrySet()) {
            CostComponent cost = entry.getKey();
            double totalCost = entry.getValue() * amount;

            if (!cost.canPay(player, totalCost)) return false;
        }

        // ФАЗА 3: ТРАНЗАКЦИЯ ОДОБРЕНА (Изменяем данные)

        /*
            Списываем средства
         */
        for (Map.Entry<CostComponent, Double> entry : finalCosts.entrySet()) {
            CostComponent cost = entry.getKey();
            double totalCost = entry.getValue() * amount;
            cost.pay(player, totalCost);
        }

        /*
            Выдаем награды
         */
        for (RewardComponent reward : offer.getComponents(RewardComponent.class)) {
            reward.reward(player, amount);
        }

        /*
            Считаем лимиты
         */
        for (LimiterComponent limiter : limiters) {
            limiter.addLimit(player, amount);
        }

        syncLimitersNetwork(limiters, player);

        return true;
    }

    // TODO: Добавить логику синхронизации лимитов между клиентами и сервером
    private static void syncLimitersNetwork(List<LimiterComponent> limiters, ServerPlayer player) { }


    /**
     * Рассчитывает финальную стоимость для выбранной группы оплаты (chosenGroupId).
     *
     * @return Map, где ключ - компонент стоимости, а значение - финальная цена после скидок.
     */
    public static Map<CostComponent, Double> calculateFinalCosts(
           final ShopOffer offer,
           final MinecraftServer server,
           final String chosenGroupId
    ) {
        final Map<CostComponent, Double> finalCosts = new HashMap<>();

        /*
            Отбираем только те компоненты стоимости, которые относятся к выбранной группе
         */
        final List<CostComponent> groupCosts = new ArrayList<>();
        final List<CostComponent> cost_components = offer.getComponents(CostComponent.class);
        for (int i = 0; i < cost_components.size(); i++) {
            final CostComponent cost = cost_components.get(i);
            final String costGroup = cost.getGroupId() != null ? cost.getGroupId() : "";
            if (costGroup.equals(chosenGroupId)) {
                groupCosts.add(cost);
            }
        }

        /*
            Если для этой группы нет цен, возвращаем пустую мапу
         */
        if (groupCosts.isEmpty()) return finalCosts;

        /*
             Собираем ID всех АКТИВНЫХ акций (PromoComponent)
         */
        final Set<String> activePromos = new HashSet<>();
        final List<PromoComponent> promo_components = offer.getComponents(PromoComponent.class);
        for (int i = 0; i < promo_components.size(); i++) {
            PromoComponent promo = promo_components.get(i);
            if (promo.isActive(server)) {
                activePromos.add(promo.getPromoId() != null ? promo.getPromoId() : "");
            }
        }

        /*
            Отбираем Эффекты, которые применимы прямо сейчас к этой группе
         */
        final List<PromoEffectComponent> activeEffects = new ArrayList<>();
        if (!activePromos.isEmpty()) { // Если нет активных акций, эффекты можно не искать
            final List<PromoEffectComponent> promo_effect_components = offer.getComponents(PromoEffectComponent.class);
            for (int i = 0; i < promo_effect_components.size(); i++) {
                final PromoEffectComponent effect = promo_effect_components.get(i);
                if (effect.canApply(activePromos, chosenGroupId)) {
                    activeEffects.add(effect);
                }
            }
        }

        /*
            Считаем финальную цену для КАЖДОГО компонента стоимости в группе
         */
        for (int j = 0; j < groupCosts.size(); j++) {
            final CostComponent cost = groupCosts.get(j);
            double currentPrice = cost.getBaseAmount();

            /*
                Прогоняем базовую цену через все активные эффекты (скидки/модификаторы)
             */
            for (int i = 0; i < activeEffects.size(); i++) {
                final PromoEffectComponent effect = activeEffects.get(i);
                currentPrice = effect.applyPrice(currentPrice, activePromos, effect.getApplyGroups());
            }

            /*
                Защита от отрицательных цен
             */
            if (currentPrice < 0) currentPrice = 0;

            finalCosts.put(cost, currentPrice);
        }

        ShopScriptEvents.SCRIPT_CALCULATE_PRICE_EVENT.invoker().invoke(offer, server, chosenGroupId, finalCosts);
        ShopServerEvents.CALCULATE_PRICE_EVENT.invoker().invoke(offer, server, chosenGroupId, finalCosts);

        return finalCosts;
    }
}
