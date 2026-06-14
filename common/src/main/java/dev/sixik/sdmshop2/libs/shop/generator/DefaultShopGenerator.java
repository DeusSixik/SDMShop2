package dev.sixik.sdmshop2.libs.shop.generator;

import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.sdmeconomy.IExternalCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyCurrencyRegistry;
import dev.sixik.sdmshop2.libs.sdmeconomy.custom_currency.ExternalItemCurrency;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.builder.ShopBuilder;
import dev.sixik.sdmshop2.libs.shop.builder.ShopOfferBuilder;
import dev.sixik.sdmshop2.libs.shop.components.ItemRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.NameComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DefaultShopGenerator {

    public static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "default");

    private static final String EMERALD_MONEY = "emerald";
    private static final String GOLD_MONEY = "gold";
    private static final String DIAMOND_MONEY = "diamond";
    private static final String NETHERITE_MONEY = "netherite";

    private static final String RESOURCES_GROUP = "resources";
    private static final String TOOLS_GROUP = "tools";
    public static final String RARES_GROUP = "rares";
    public static final String BLOCKS_GROUP = "blocks";
    public static final String NATURE_GROUP = "nature";
    public static final String EXCHANGE_GROUP = "exchange";

    public static void registerDefault() {
        if(ShopTable.Instance == null) {
            throw new IllegalStateException("ShopTable is not initialized");
        }

        if(ShopTable.Instance.getShop(ID) != null) {
            return;
        }

        try {
            registerUnknownCurrencies();
            ShopTable.Instance.addShop(generate());
        } catch (Exception e) {
            SDMShop2.LOGGER.error("Failed to register default shop", e);
        }
    }

    private static void checkOrRegisterCurrency(IExternalCurrency cur) {
        IExternalCurrency currency = SDMEconomyCurrencyRegistry.getCurrency(cur.getId());
        if(currency == null) {
            SDMEconomyCurrencyRegistry.registerAndSaveCurrency(cur);
        }
    }

    private static void registerUnknownCurrencies() {
        checkOrRegisterCurrency(new ExternalItemCurrency(EMERALD_MONEY, new ItemStack(Items.EMERALD)));
        checkOrRegisterCurrency(new ExternalItemCurrency(GOLD_MONEY, new ItemStack(Items.GOLD_INGOT)));
        checkOrRegisterCurrency(new ExternalItemCurrency(DIAMOND_MONEY, new ItemStack(Items.DIAMOND)));
        checkOrRegisterCurrency(new ExternalItemCurrency(NETHERITE_MONEY, new ItemStack(Items.NETHERITE_INGOT)));
    }

    private static ShopInstance generate() {
        ShopBuilder builder = ShopBuilder.builder(ResourceLocation.tryBuild("sdm", "default"));

        addSimpleItem(builder, Items.IRON_INGOT,    3, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.COPPER_INGOT,  6, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.GOLD_INGOT,    1, 2, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.DIAMOND,       1, 4, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.NETHERITE_SCRAP, 1, 64, RESOURCES_GROUP, EMERALD_MONEY)
//                .addPrice(GOLD_MONEY, 16).addPrice(DIAMOND_MONEY, 4);
//        addSimpleItem(builder, Items.FLINT, 16, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.FLINT, 8, 2, RESOURCES_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.PHANTOM_MEMBRANE, 4, 2, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.SUGAR, 8, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.SUGAR, 2, 4, RESOURCES_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.STRING, 8, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.STRING, 2, 4, RESOURCES_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.LAPIS_LAZULI, 8, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.LAPIS_LAZULI, 2, 4, RESOURCES_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.COAL, 8, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.COAL, 2, 4, RESOURCES_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.AMETHYST_SHARD, 4, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.QUARTZ, 8, 1, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.ECHO_SHARD, 2, 4, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.BLAZE_ROD, 2, 2, RESOURCES_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.SHULKER_SHELL, 1, 8, RESOURCES_GROUP, EMERALD_MONEY);
//
//        addSimpleItem(builder, Items.GLASS, 16, 2, BLOCKS_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.OBSIDIAN, 4, 4, BLOCKS_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.GLOWSTONE, 8, 4, BLOCKS_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.SEA_LANTERN, 4, 5, BLOCKS_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.END_STONE, 16, 2, BLOCKS_GROUP, EMERALD_MONEY);
//
//        addSimpleItem(builder, Items.SLIME_BALL, 2, 3, NATURE_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.LEATHER, 8, 2, NATURE_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.SPONGE, 1, 12, NATURE_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.MYCELIUM, 8, 4, NATURE_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.NAME_TAG, 1, 10, NATURE_GROUP, EMERALD_MONEY);
//
//        addSimpleItem(builder, Items.EXPERIENCE_BOTTLE, 16, 1, RARES_GROUP, DIAMOND_MONEY);
//        addSimpleItem(builder, Items.ENCHANTED_GOLDEN_APPLE, 1, 32, RARES_GROUP, DIAMOND_MONEY);
//        addSimpleItem(builder, Items.WITHER_SKELETON_SKULL, 1, 10, RARES_GROUP, DIAMOND_MONEY);
//        addSimpleItem(builder, Items.ELYTRA, 1, 64, RARES_GROUP, DIAMOND_MONEY);
        builder.addOffer(
                ShopOfferBuilder.builder(RARES_GROUP)
                        .addComponent(new ItemRewardComponent(new ItemStack(Items.DRAGON_EGG), 1))
                        .addComponent(new NameComponent("My World Name"))
                        .addPrice(NETHERITE_MONEY, 2)
                        .addPrice(DIAMOND_MONEY, 10)
                        .addPrice(GOLD_MONEY, 32)
                        .end()
        );

//        addSimpleItem(builder, Items.GOLD_INGOT, 1, 4.0, EXCHANGE_GROUP, EMERALD_MONEY);
//        addSimpleItem(builder, Items.EMERALD, 4, 1.0, EXCHANGE_GROUP, GOLD_MONEY);
//
//        addSimpleItem(builder, Items.DIAMOND, 1, 4.0, EXCHANGE_GROUP, GOLD_MONEY);
//        addSimpleItem(builder, Items.GOLD_INGOT, 4, 1.0, EXCHANGE_GROUP, DIAMOND_MONEY);
//
//        addSimpleItem(builder, Items.NETHERITE_SCRAP, 1, 8.0, EXCHANGE_GROUP, DIAMOND_MONEY);
//        addSimpleItem(builder, Items.DIAMOND, 8, 1.0, EXCHANGE_GROUP, NETHERITE_MONEY);

        return builder.mustSave().end();
    }

    /**
     * Базовый вспомогательный метод для добавления оффера.
     */
    private static ShopOfferBuilder addSimpleItem(ShopBuilder shop, Item item, int amount, double price, String group, String currency) {
        ShopOfferBuilder builder = ShopOfferBuilder.builder(group)
                .addPrice(currency, price)
                .addComponent(new ItemRewardComponent(new ItemStack(item), amount));
        shop.addOffer(
                builder
        );
        return builder;
    }

    private static ShopOfferBuilder addSimpleItem(ShopBuilder shop, Item item, int amount, double price, String group) {
        return addSimpleItem(shop, item, amount, price, group, EMERALD_MONEY);
    }

    private static ShopOfferBuilder addSimpleItem(ShopBuilder shop, Item item, double price, String group) {
       return addSimpleItem(shop, item, 1, price, group, EMERALD_MONEY);
    }
}
