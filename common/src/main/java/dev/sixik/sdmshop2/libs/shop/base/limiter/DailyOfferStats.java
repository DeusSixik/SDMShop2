package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DailyOfferStats {

    @Getter
    private final String date;
    private final Map<UUID, ShopLimiterOfferData> offerSales = new ConcurrentHashMap<>();

    @Setter
    private ShopLimiterUpdate update = () -> {};

    public DailyOfferStats(String date) {
        this.date = date;
    }

    public DailyOfferStats(JsonObject json) {
        this(json.get("date").getAsString());

        if (json.has("sales")) {
            JsonObject sales = json.getAsJsonObject("sales");
            sales.entrySet().forEach(entry -> {
                UUID id = UUID.fromString(entry.getKey());
                offerSales.put(id, new ShopLimiterOfferData(entry.getValue().getAsJsonObject()));
            });
        }
    }

    public void addSale(UUID offerId, int amount) {
        ShopLimiterOfferData data = offerSales.computeIfAbsent(offerId, s -> new ShopLimiterOfferData(s) {
            @Override
            public void markPurchased(long time) { }
        });
        data.add(amount);
        update.onUpdate();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("date", date);

        if(!offerSales.isEmpty()) {
            JsonObject salesJson = new JsonObject();
            offerSales.forEach((id, data) -> salesJson.add(id.toString(), data.toJson()));
            json.add("sales", salesJson);
        }

        return json;
    }
}
