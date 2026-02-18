package dev.sixik.sdmshop2.libs.sdmeconomy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SDMEconomyServiceClient extends SDMEconomyService {

    protected final BankAccount bankAccount;

    public SDMEconomyServiceClient() {
        this.bankAccount = new BankAccount(Minecraft.getInstance().player);
    }

    public SDMEconomyServiceClient(BankAccount account) {
        this.bankAccount = account;
    }

    public BankAccount getAccount() {
        return bankAccount;
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
