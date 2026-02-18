package dev.sixik.sdmshop2.libs.sdmeconomy;

import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SDMEconomyServiceClient extends SDMEconomyService {

    public static Map<ResourceLocation, IExternalCurrency> SERVER_CURRENCY = new HashMap<>();

    public static Map<ResourceLocation, IExternalCurrency> getAllCurrencies() {
        Map<ResourceLocation, IExternalCurrency> map = new HashMap<>(SDMEconomyCurrencyRegistry.getCurrenciesMap());
        if(SDMEconomyPlatform.server == null)
            map.putAll(SDMEconomyServiceClient.SERVER_CURRENCY);
        return map;
    }

    @Getter
    private static final SDMEconomyServiceClient InstanceClient = new SDMEconomyServiceClient();

    @Getter
    protected BankAccount bankAccount;

    public SDMEconomyServiceClient() {
        this.bankAccount = new BankAccount(Minecraft.getInstance().player);
    }

    public SDMEconomyServiceClient(BankAccount account) {
        this.bankAccount = account;
    }

    @Override
    @Deprecated
    public BankAccount getAccount(UUID gameProfileId) {
        return bankAccount;
    }

    @Override
    public void unloadPlayer(UUID gameProfileId) { }

    @Override
    public void saveAllDirty() { }

    @Override
    protected void saveAccount(UUID gameProfileId, BankAccount account) { }

    @Override
    protected BankAccount loadAccountFromDisk(UUID gameProfileId) {
        return null;
    }
}
