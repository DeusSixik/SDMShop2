package dev.sixik.sdmshop2.libs.shop.base.repositoryManager;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.OperationType;
import dev.sixik.sdmshop2.libs.shop.base.repository.MongoGenericRepository;
import dev.sixik.sdmshop2.libs.shop.base.repository.Repository;
import dev.sixik.sdmshop2.libs.shop.config.ShopDataStorageConfig;
import lombok.Getter;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MongoRepositoryManager extends RepositoryManager{

    protected static final Logger LOGGER = LoggerFactory.getLogger(MongoRepositoryManager.class);

    protected MongoClient client;
    private MongoDatabase database;

    protected final String connectionString;
    protected final String dbName;

    @Getter
    protected final String serverIdentifier;

    private final Map<String, List<MongoChangeListener>> listeners = new ConcurrentHashMap<>();

    public MongoRepositoryManager(ShopDataStorageConfig.MongoConfig config) {
        this(config.uri, config.database, config.serverName);
    }

    public MongoRepositoryManager(String uri, String db, String name) {
        this.connectionString = uri;
        this.dbName = db;

        /*
            Если имя сервера в конфиге пустое - генерим UUID, иначе берем из конфига
         */
        this.serverIdentifier = (name == null || name.isEmpty())
                ? UUID.randomUUID().toString()
                : name;
    }

    /**
     * Создает Ref для конкретной коллекции и автоматически регистрирует слушателя.
     */
    public MongoRef createRef(String collectionName, MongoChangeListener listener) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        listeners.computeIfAbsent(collectionName, k -> new CopyOnWriteArrayList<>()).add(listener);
        return new MongoRef(this, collection, collectionName);
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
        this.database = client.getDatabase(dbName);

        startWatchingDatabase();
    }

    private void startWatchingDatabase() {
        Thread watchThread = new Thread(() -> {
            try (MongoCursor<ChangeStreamDocument<Document>> cursor = database.watch()
                    .fullDocument(FullDocument.UPDATE_LOOKUP)
                    .cursor()) {

                LOGGER.info("✅ SUCCESS: Global DB Watcher started for database '{}'", dbName);

                while (cursor.hasNext()) {
                    ChangeStreamDocument<Document> change = cursor.next();

                    if (change.getNamespace() == null) continue;
                    String changedCollection = change.getNamespace().getCollectionName();

                    List<MongoChangeListener> targets = listeners.get(changedCollection);
                    if (targets == null || targets.isEmpty()) continue;

                    BsonDocument documentKey = change.getDocumentKey();
                    if (documentKey == null || !documentKey.containsKey("_id")) continue;
                    String rawId = documentKey.getString("_id").getValue().toString();

                    Document fullDoc = change.getFullDocument();
                    if (fullDoc != null && fullDoc.containsKey("last_updated_by")) {
                        if (serverIdentifier.equals(fullDoc.getString("last_updated_by"))) {
                            continue; // Это наше изменение, пропускаем
                        }
                    }

                    OperationType opType = change.getOperationType();
                    for (MongoChangeListener listener : targets) {
                        if (opType == OperationType.INSERT || opType == OperationType.REPLACE || opType == OperationType.UPDATE) {
                            listener.onRemoteUpdate(rawId);
                        } else if (opType == OperationType.DELETE) {
                            listener.onRemoteDelete(rawId);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Global MongoDB Watcher Thread stopped!", e);
            }
        }, "SDM-Global-Watcher-" + dbName);

        watchThread.setDaemon(true);
        watchThread.start();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public <K, V> Repository<K, V> createRepository(@Nullable Path custom, @NonNull String collectionName, RepoDefinition<K, V> def) {
        return new MongoGenericRepository<>(
                this,
                collectionName,
                def.keyToString(),
                def.stringToKey(),
                def.extractKey(),
                def.serializer(),
                def.deserializer()
        );
    }

    public record MongoRef(MongoRepositoryManager manager, MongoCollection<Document> collection, String collectionName) { }

    public interface MongoChangeListener {
        void onRemoteUpdate(String rawId);
        void onRemoteDelete(String rawId);
    }
}
