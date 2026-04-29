package dev.sixik.sdmshop2.libs.shop.config;

import net.shadowking21.shadowconfig.annotation.ConfigComment;

public class ShopDataStorageConfig {

//    @ConfigComment("Values: JSON - The data will be saved in the folder \"config/sdm/shop/shops/*\", MONGODB - The data will be stored in a database that automatically synchronizes data between multiple servers., CUSTOM - Self-written save type \n\nDefault Value: JSON")
    public StorageType type = StorageType.JSON;

    public MongoConfig mongodb = new MongoConfig();

    public enum StorageType {
        JSON,
        MONGODB,
        CUSTOM
    }

    public static class MongoConfig {

        @ConfigComment("Connection string\nLocal: mongodb://127.0.0.1:27017/?replicaSet=rs0\nCloud: mongodb+srv://user:password@cluster.mongodb.net/")
        public String uri = "mongodb://127.0.0.1:27017/?replicaSet=rs0";

        @ConfigComment("Data base name")
        public String database = "sdm_shop";

        public String shopsCollection = "shops";

        public String limiterOffersCollection = "limiter_offers";

        public String limiterPlayersCollection = "limiter_players";

        public String dailyStatsCollection = "daily_stats";

        @ConfigComment("A unique name for this server (eg: \"survival_1\", \"lobby\").\nUsed for echo protection (to prevent the server from updating itself).\nIf left blank, a random UUID will be generated.")
        public String serverName = "server_1";
    }
}
