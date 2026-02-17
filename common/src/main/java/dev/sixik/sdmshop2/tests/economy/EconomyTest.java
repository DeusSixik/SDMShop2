package dev.sixik.sdmshop2.tests.economy;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.sixik.sdmshop2.libs.sdmeconomy.BankAccount;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyCurrencyRegistry;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;

public class EconomyTest {

    private static TestSDMCoin sdmCoin = new TestSDMCoin();

    public static void init() {

        PlayerEvent.DROP_ITEM.register((EconomyTest::drop));
    }

    private static EventResult drop(Player player, ItemEntity itemEntity) {

        BankAccount account = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());

        System.out.println(account.getBalance(sdmCoin));
        account.modify(sdmCoin, BigDecimal.ONE);

        System.out.println(SDMEconomyCurrencyRegistry.getCurrency("custom_currency").getBalance(player));

        return EventResult.interruptDefault();
    }
}
