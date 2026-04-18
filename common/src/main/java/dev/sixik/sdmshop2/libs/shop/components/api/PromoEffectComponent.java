package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.Set;

public abstract class PromoEffectComponent extends ShopComponent {

    @Getter
    @Setter
    private String targetPromoId;

    @Getter
    private final Set<String> applyGroups = new HashSet<>();

    /**
     * Проверяет, применим ли данный эффект при текущих активных акциях и выбранной группе оплаты.
     */
    public boolean canApply(Set<String> activePromos, String chosenGroupId) {
        boolean matchPromo = targetPromoId == null || targetPromoId.isEmpty() || activePromos.contains(targetPromoId);
        boolean matchGroup = applyGroups.isEmpty() || applyGroups.contains(chosenGroupId);
        return matchPromo && matchGroup;
    }

    public double applyPrice(double input, Set<String> activePromo, Set<String> activeGroups) {
        return input;
    }

    public int applyAmount(int input, Set<String> activePromo) {
        return input;
    }

    public final void applyGroup(String groupId) {
        applyGroups.add(groupId);
    }

    public final boolean removeGroup(String groupId) {
        return applyGroups.remove(groupId);
    }

    @Override
    public void additionalSerialize(JsonObject json) {
        if(targetPromoId != null && !targetPromoId.isEmpty())
            json.addProperty("target_promo_id", targetPromoId);

        JsonArray array = new JsonArray();
        applyGroups.forEach(array::add);
        json.add("apply_groups", array);
    }

    @Override
    public void additionalDeserialize(JsonObject json) {
        if(json.has("target_promo_id"))
            targetPromoId = json.get("target_promo_id").getAsString();

        if(json.has("apply_groups")) {
            JsonArray array = json.getAsJsonArray("apply_groups");
            array.forEach(element -> applyGroups.add(element.getAsString()));
        }
    }

    @Override
    public void additionalToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(targetPromoId != null ? targetPromoId : "");

        buf.writeVarInt(applyGroups.size());
        applyGroups.forEach(buf::writeUtf);
    }

    @Override
    public void additionalFromNetwork(FriendlyByteBuf buf) {
        targetPromoId = buf.readUtf();

        applyGroups.clear();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            applyGroups.add(buf.readUtf());
        }
    }
}
