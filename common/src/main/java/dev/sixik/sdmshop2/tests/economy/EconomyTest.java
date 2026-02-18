package dev.sixik.sdmshop2.tests.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntry;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.client.screens.ShopScreenManager;
import dev.sixik.sdmshop2.libs.shop.components.CategoryComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopEntriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncBridge;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class EconomyTest {

    public static void init() {

        PlayerEvent.DROP_ITEM.register((EconomyTest::drop));
    }

    private static EventResult drop(Player player, ItemEntity itemEntity) {

        new ShopScreenManager().openGui();
        return EventResult.interruptDefault();
    }

    public static void test() {

        ShopInstance manager = ShopInstance.createManager(ResourceLocation.tryBuild("sdm", "test_manager"), true);

        ShopEntriesContainerComponent entriesComponent = manager.getComponent(ShopEntriesContainerComponent.class).get();

        UUID categoryId = UUID.randomUUID();

        for (int i = 0; i < 20; i++) {
            ShopEntry entry = ShopEntry.createEntry(UUID.randomUUID(), true);
            entry.getComponent(CategoryComponent.class).get().setUuid(categoryId);
            entriesComponent.addEntry(entry);
        }

        ShopCategoriesContainerComponent categoryManager = manager.getComponent(ShopCategoriesContainerComponent.class).get();
        categoryManager.reindex();

        System.out.println("Categorise Entries: " + categoryManager.getCategoriesEntry(categoryId).size());



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
