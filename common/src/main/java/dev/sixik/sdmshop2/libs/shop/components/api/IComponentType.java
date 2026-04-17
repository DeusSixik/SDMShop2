package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IComponentType<T extends ShopComponent> {

    ResourceLocation getId();

    JsonObject serialize(T component);

    T deserialize(JsonObject json);

    void toNetwork(FriendlyByteBuf buf, T component);

    T fromNetwork(FriendlyByteBuf buf);

    T createDefault();
}
