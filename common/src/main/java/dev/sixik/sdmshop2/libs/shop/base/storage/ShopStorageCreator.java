package dev.sixik.sdmshop2.libs.shop.base.storage;

import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.config.ShopDataStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopStorageCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopStorageCreator.class);

    public static ShopStorage createStorage() {
        final var data = SDMShop2.getDataStorageConfig();
        final ShopDataStorageConfig config = data.getCurrentConfig();

        return switch (config.type) {
            case JSON -> {
                LOGGER.info("Using local JSON storage for SDM Shop 2");
                yield new JsonShopStorage();
            }
            case MONGODB -> {
                LOGGER.info("Using MongoDB storage for SDM Shop 2");
                yield new MongoDbShopStorage(config.mongodb);
            }
            case CUSTOM -> null;
        };
    }
}
