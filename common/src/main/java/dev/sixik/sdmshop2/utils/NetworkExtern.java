package dev.sixik.sdmshop2.utils;

import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NetworkExtern {

    public static void writeOffersUUIDs(FriendlyByteBuf buf, Collection<ShopOffer> offers) {
        buf.writeVarInt(offers.size());
        for (ShopOffer offer : offers) {
            buf.writeUUID(offer.getUUID());
        }
    }

    public static <ComponentType extends ShopComponent, ValueType> Map<ComponentType, ValueType> readMap(
            FriendlyByteBuf buf, List<ComponentType> getterList, FriendlyByteBuf.Reader<ValueType> valueReader
    ) {
        return readMap(buf, getterList, valueReader, "ShopNetworkApi");
    }

    public static <ComponentType extends ShopComponent, ValueType> Map<ComponentType, ValueType> readMap(
            FriendlyByteBuf buf, List<ComponentType> getterList, FriendlyByteBuf.Reader<ValueType> valueReader, String debugLabel
    ) {
        return buf.readMap(
                data_buf -> {
                    int index = data_buf.readVarInt();
                    if (index < 0 || index >= getterList.size()) {
                        SDMShop2.LOGGER.error("[{}] Desync! Component index {} out of bounds.", debugLabel, index);
                        return null;
                    }
                    return getterList.get(index);
                },
                valueReader
        );
    }

    public static <ComponentType extends ShopComponent, ValueType> void writeMap(
            FriendlyByteBuf buf, Map<ComponentType, ValueType> inMap, List<ComponentType> indexList, FriendlyByteBuf.Writer<ValueType> valueWriter
    ) {
        buf.writeVarInt(inMap.size());
        for (Map.Entry<ComponentType, ValueType> entry : inMap.entrySet()) {
            int componentIndex = indexList.indexOf(entry.getKey());
            buf.writeVarInt(componentIndex);
            valueWriter.accept(buf, entry.getValue());
        }
    }
}
