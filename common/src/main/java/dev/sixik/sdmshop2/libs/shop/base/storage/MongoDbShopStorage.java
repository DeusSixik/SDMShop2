package dev.sixik.sdmshop2.libs.shop.base.storage;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.OperationType;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.config.ShopDataStorageConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.BsonDocument;
import org.bson.Document;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class MongoDbShopStorage extends ShopStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbShopStorage.class);

    private MongoClient client;
    private MongoCollection<Document> collection;

    private final String connectionString;
    private final String dbName;
    private final String collectionName;
    private final String serverIdentifier;

    public MongoDbShopStorage(ShopDataStorageConfig.MongoConfig config) {
        this(config.uri, config.database, config.collection, config.serverName);
    }

    public MongoDbShopStorage(String uri, String db, String coll, String name) {
        this.connectionString = uri;
        this.dbName = db;
        this.collectionName = coll;

        // Если имя сервера в конфиге пустое - генерим UUID, иначе берем из конфига
        this.serverIdentifier = (name == null || name.isEmpty())
                ? UUID.randomUUID().toString()
                : name;
    }

    @Override
    public void init() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        this.client = MongoClients.create(settings);
        this.collection = client.getDatabase(dbName).getCollection(collectionName);

        startWatchingChanges();
    }

    private void startWatchingChanges() {
        Thread watchThread = new Thread(() -> {
            /*
                Настраиваем стрим так, чтобы он всегда подтягивал актуальную версию документа
             */
            try (MongoCursor<ChangeStreamDocument<Document>> cursor = collection.watch()
                    .fullDocument(FullDocument.UPDATE_LOOKUP)
                    .cursor()
            ) {
                LOGGER.info("✅ SUCCESS: Started watching MongoDB changes for cross-server sync.");

                while (cursor.hasNext()) {
                    ChangeStreamDocument<Document> change = cursor.next();
                    OperationType opType = change.getOperationType();

                    /*
                        Получаем _id измненного документа
                     */
                    BsonDocument documentKey = change.getDocumentKey();
                    if (documentKey == null || !documentKey.containsKey("_id")) continue;

                    String rawId = documentKey.getString("_id").getValue();
                    ResourceLocation shopId = new ResourceLocation(rawId);


                    Document fullDoc = change.getFullDocument();

                    /*
                        Проверка отправителя новвых данных. Чтобы не реагировать на свои же данные
                     */
                    if (fullDoc != null && fullDoc.containsKey("last_updated_by")) {
                        String updatedBy = fullDoc.getString("last_updated_by");

                        if (serverIdentifier.equals(updatedBy)) {
                            continue;
                        }
                    }

                    if (ShopTable.Instance != null) {
                        if (opType == OperationType.INSERT || opType == OperationType.REPLACE || opType == OperationType.UPDATE) {
                            LOGGER.info("🔄 DB Update for '{}'. Reloading only this shop...", shopId);
                            ShopTable.Instance.reloadShop(shopId);

                        } else if (opType == OperationType.DELETE) {
                            LOGGER.info("🗑️ DB Delete for '{}'. Removing from local cache...", shopId);
                            ShopTable.Instance.removeShopFromCache(shopId);
                        }
                    }
                }
            } catch (MongoException | IllegalStateException e) {
                LOGGER.info("MongoDB watch stream closed.");
            } catch (Exception e) {
                LOGGER.error("Unexpected error in MongoDB Change Stream!", e);
            }
        }, "SDM-Mongo-Watcher");

        watchThread.setDaemon(true);
        watchThread.start();
    }

    @Override
    @Nullable
    public ShopInstance load(ResourceLocation id) {
        Document doc = collection.find(Filters.eq("_id", id.toString())).first();
        if (doc == null) {
            return null;
        }

        try {
            JsonObject json = JsonParser.parseString(doc.toJson()).getAsJsonObject();
            return ShopInstance.fromJson(json);
        } catch (Exception e) {
            LOGGER.error("Failed to load shop from MongoDB: {}", id, e);
            return null;
        }
    }

    @Override
    public Map<ResourceLocation, ShopInstance> loadAll() {
        Map<ResourceLocation, ShopInstance> map = new Object2ObjectOpenHashMap<>();

        for (Document doc : collection.find()) {
            try {
                JsonObject json = JsonParser.parseString(doc.toJson()).getAsJsonObject();
                ShopInstance shop = ShopInstance.fromJson(json);
                map.put(shop.getId(), shop);
            } catch (Exception e) {
                LOGGER.error("Failed to load shop from MongoDB: {}", doc.getString("_id"), e);
            }
        }
        return map;
    }

    @Override
    public void save(ShopInstance shop) {
        String jsonString = shop.serialize().toString();
        Document doc = Document.parse(jsonString);

        String stringId = shop.getId().toString();
        doc.put("_id", stringId);
        doc.put("last_updated_by", serverIdentifier);

        /*
            Используем upsert: если магазина нет - создаст, если есть - полностью заменит.
         */
        collection.replaceOne(
                Filters.eq("_id", stringId),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void delete(ResourceLocation id) {
        collection.deleteOne(Filters.eq("_id", id.toString()));
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
