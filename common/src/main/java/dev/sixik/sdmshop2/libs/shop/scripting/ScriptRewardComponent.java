package dev.sixik.sdmshop2.libs.shop.scripting;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.RewardComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Компонент награды, который выдает награду через скриптовое событие.
 */
public class ScriptRewardComponent extends RewardComponent {

    /**
     * Тип компонента для регистрации в системе магазинов.
     */
    public static final IComponentType<ScriptRewardComponent> TYPE = new Type();

    /**
     * Идентификатор скрипта, который будет вызван для выдачи награды.
     */
    @Getter
    @Setter
    private String scripId = "";

    /**
     * Конструктор по умолчанию.
     */
    public ScriptRewardComponent() { }

    /**
     * Конструктор с указанием идентификатора скрипта.
     *
     * @param scripId Идентификатор скрипта.
     */
    public ScriptRewardComponent(String scripId) {
        this.scripId = scripId;
    }

    /**
     * Выдает награду игроку.
     * Вызывает скриптовое событие {@link ShopScriptEvents#SCRIP_REWARD_EVENT}.
     *
     * @param player Игрок, получающий награду.
     * @param amount Количество награды (множитель).
     */
    @Override
    public void reward(ServerPlayer player, int amount) {
        ShopScriptEvents.SCRIP_REWARD_EVENT.invoker().invoke(player, amount, this, scripId);
    }

    /**
     * Возвращает тип компонента.
     *
     * @return Тип компонента.
     */
    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    /**
     * Определяет, нужно ли синхронизировать этот компонент с клиентом.
     * Скриптовые награды выполняются на сервере, поэтому синхронизация не требуется.
     *
     * @return false.
     */
    @Override
    public boolean shouldSync() {
        return false;
    }

    /**
     * Класс для регистрации типа компонента в системе магазинов.
     */
    private static class Type implements IComponentType<ScriptRewardComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "reward_script");

        /**
         * Возвращает уникальный идентификатор типа компонента.
         *
         * @return ResourceLocation идентификатор.
         */
        @Override
        public ResourceLocation getId() {
            return ID;
        }

        /**
         * Сериализует компонент в JSON объект.
         *
         * @param component Компонент для сериализации.
         * @return JsonObject с данными компонента.
         */
        @Override
        public JsonObject serialize(ScriptRewardComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("script_id", component.scripId);
            return json;
        }

        /**
         * Десериализует компонент из JSON объекта.
         *
         * @param json JsonObject с данными компонента.
         * @return Новый экземпляр ScriptRewardComponent.
         */
        @Override
        public ScriptRewardComponent deserialize(JsonObject json) {
            return new ScriptRewardComponent(json.get("script_id").getAsString());
        }

        /**
         * Записывает данные компонента в сетевой буфер.
         * Не поддерживается, так как компонент не синхронизируется.
         *
         * @throws UnsupportedOperationException всегда.
         */
        @Override
        public void toNetwork(FriendlyByteBuf buf, ScriptRewardComponent component) {
            throw new UnsupportedOperationException();
        }

        /**
         * Читает данные компонента из сетевого буфера.
         * Не поддерживается, так как компонент не синхронизируется.
         *
         * @throws UnsupportedOperationException всегда.
         */
        @Override
        public ScriptRewardComponent fromNetwork(FriendlyByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        /**
         * Создает экземпляр компонента по умолчанию.
         *
         * @return Новый экземпляр ScriptRewardComponent.
         */
        @Override
        public ScriptRewardComponent createDefault() {
            return new ScriptRewardComponent();
        }
    }
}
