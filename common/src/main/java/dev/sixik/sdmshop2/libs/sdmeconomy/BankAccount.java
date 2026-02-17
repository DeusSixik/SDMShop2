package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.mojang.authlib.GameProfile;
import dev.sixik.sdmshop2.utils.NbtExtern;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BankAccount {

    /**
     * Владелец Аккаунта. Это всегда ID {@link GameProfile}
     */
    @Getter
    private final UUID gameProfileOwnerId;

    private final Map<ResourceLocation, BigDecimal> balances = new ConcurrentHashMap<>();

    @Getter
    private boolean dirty = false;

    public BankAccount(Player player) {
        this(player.getGameProfile());
    }

    public BankAccount(GameProfile profile) {
        this(profile.getId());
    }

    public BankAccount(UUID gameProfileId) {
        this.gameProfileOwnerId = gameProfileId;
    }

    /**
     * Возвращает баланс игрока или стандартное значение валюты {@link IStoredCurrency#getDefaultBalance()} если у игрока нет этой валюты
     */
    public BigDecimal getBalance(IStoredCurrency currency) {
        return balances.getOrDefault(currency.getId(), currency.getDefaultBalance());
    }

    /**
     * Устанавливает количество конкретной валюты
     */
    public void setBalance(IStoredCurrency currency, BigDecimal amount) {
        balances.put(currency.getId(), amount);
        markDirty();
    }

    /**
     * Изменяет количество конкретной валюты
     * @param amount Сколько добавить или убавить
     */
    public void modify(IStoredCurrency currency, BigDecimal amount) {
        balances.merge(currency.getId(), amount, BigDecimal::add);
        markDirty();
    }

    /**
     * Помечает то что данные изменились и их нужно сохранить
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Помечает то что данные сохранять не нужно
     */
    public void markClean() {
        this.dirty = false;
    }

    public CompoundTag serializeNbt() {
        final CompoundTag nbt = new CompoundTag();
        final ListTag list = new ListTag();
        balances.forEach((id, val) -> {
            final CompoundTag entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.putString("val", val.toString());
            list.add(entry);
        });
        nbt.put("balances", list);
        return nbt;
    }

    public void deserializeNbt(final CompoundTag nbt) {
        final ListTag list = NbtExtern.getOrThrow(nbt, "balances");

        balances.clear();

        for (int i = 0; i < list.size(); i++) {
            final CompoundTag entry = list.getCompound(i);

            balances.put(
                    ResourceLocation.tryParse(entry.getString("id")),
                    new BigDecimal(entry.getString("val"))
            );
        }
    }
}
