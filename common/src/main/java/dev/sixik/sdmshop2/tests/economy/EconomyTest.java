package dev.sixik.sdmshop2.tests.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.sdmeconomy.BankAccount;
import dev.sixik.sdmshop2.libs.sdmeconomy.DynamicStoredCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.ItemRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopOffersContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyCostComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.conditions.PromoTimeComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.effects.DiscountComponent;
import dev.sixik.sdmshop2.libs.shop.generator.DefaultShopGenerator;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncBridge;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import dev.sixik.sdmshop2.libs.shop.processors.ShopTransactionProcessor;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptConditionComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptRewardComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class EconomyTest {

    public static void init() {

        PlayerEvent.DROP_ITEM.register((EconomyTest::drop));
    }

    private static EventResult drop(Player player, ItemEntity itemEntity) {

        ShopInstance shop = ShopTable.Instance.getShop(DefaultShopGenerator.ID);
        if(shop == null) {
            DefaultShopGenerator.registerDefault();
            shop = ShopTable.Instance.getShop(DefaultShopGenerator.ID);
        }

        ShopNetworkManager.sendShopDataAndOpen(shop, (ServerPlayer) player);
        return EventResult.interruptDefault();
    }

    public static void commandTest(ServerPlayer player) {

        try {
            ResourceLocation debug = ResourceLocation.tryBuild("sdm", "debug");

            DynamicStoredCurrency cur = new DynamicStoredCurrency(debug);
            BankAccount account = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());

            if (!account.hasMoney(cur))
                account.setBalance(cur, BigDecimal.valueOf(300));

            ShopInstance manager = ShopInstance.createManager(debug, true);

            ShopOffersContainerComponent entriesComponent = manager
                    .getComponent(ShopOffersContainerComponent.class).get();

            // Собираем товар который игрок покупает
            ShopOffer entry = ShopOffer.create(UUID.randomUUID(), true);
            entry.getComponent(CatalogComponent.class).get().setUuid(UUID.randomUUID());
            entriesComponent.addEntry(entry);

            // Игрок платит
            entry.addComponent(new MoneyCostComponent(debug, 5));
            entry.addComponent(new MoneyCostComponent(null, 50)).setGroupId("group1");
            entry.addComponent(new ItemRewardComponent(Items.BEDROCK.getDefaultInstance(), 5));
            entry.addComponent(new CommandRewardComponent("/give {player} diamond", "test"));

            entry.addComponent(new PromoTimeComponent(PromoTimeComponent.TimeMode.SERVER_TICKS, 1000, 5000));
            entry.addComponent(new DiscountComponent(50)).applyGroup("group1S");

            entry.addComponent(new ScriptRewardComponent("test_script_1"));
            entry.addComponent(new ScriptConditionComponent("test_script_2"));

            boolean result = ShopTransactionProcessor.executePlayerPurchase(entry, player, "", 1);
            System.out.println("Result: " + result);

            save(manager);
        } catch (Exception e) {
            SDMShop2.LOGGER.error("Error while command test", e);
        }
    }

    public static void test() {

        ShopInstance manager = ShopInstance.createManager(ResourceLocation.tryBuild("sdm", "test_manager"), false);

        ShopOffersContainerComponent entriesComponent = manager.getComponent(ShopOffersContainerComponent.class).get();

        UUID categoryId = UUID.randomUUID();

        for (int i = 0; i < 20; i++) {
            ShopOffer entry = ShopOffer.create(UUID.randomUUID(), true);
            entry.getComponent(CatalogComponent.class).get().setUuid(categoryId);
            entriesComponent.addEntry(entry);
        }
        manager.initializeServerOnlyComponents();

        ShopCategoriesContainerComponent categoryManager = manager.getComponent(ShopCategoriesContainerComponent.class).get();
//        categoryManager.reindex();

        System.out.println("Categorise Entries: " + categoryManager.getCatalogsEntry(categoryId).size());



        AsyncBridge.askPlayer(SDMEconomyPlatform.server.getPlayerList().getPlayers().get(0), AsyncServerTasks.SEND_SHOP_DATA, buf ->{
            manager.serializeNetwork(buf);
            return buf;
        });
    }

    public static void save(ShopInstance manager) {
        final Path dir = SDMEconomyPlatform.getConfigDir();
        if (!Files.exists(dir)) {
            try { Files.createDirectories(dir); } catch (IOException e) { e.printStackTrace(); }
        }

        final File filePath = dir.resolve("Test.json").toFile();

        // 1. Настраиваем Gson (PrettyPrinting делает JSON читаемым для человека)
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping() // Чтобы не экранировал символы типа < > =
                .create();

        // 2. Собираем данные в JsonObject
        // Тут мы предполагаем, что у твоего ShopManager или компонентов есть метод serialize()
        // Если его нет, мы собираем структуру вручную:
        JsonElement root = manager.serialize();

        // 3. Записываем в файл
        try (FileWriter writer = new FileWriter(filePath)) {
            // Gson сам всё запишет в writer
            gson.toJson(root, writer);

            System.out.println("Saved successfully to " + filePath.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
