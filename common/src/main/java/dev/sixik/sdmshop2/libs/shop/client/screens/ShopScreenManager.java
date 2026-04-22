package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sixik.sdmshop2.libs.shop.client.screens.ui.ShopMainPanel;
import net.minecraft.client.Minecraft;

public class ShopScreenManager {

    public static final ShopScreenManager INSTANCE = new ShopScreenManager();

    protected WidgetGroup createGui() {
        return new ShopMainPanel();
    }

    public void openGui() {

        RenderSystem.recordRenderCall(() -> {
            final var minecraft = Minecraft.getInstance();
            final var entityPlayer = minecraft.player;

            ModularUI ui = new ModularUI(createGui(), IUIHolder.EMPTY, entityPlayer);
            ui.setFullScreen();
            ui.initWidgets();
            ModularUIGuiContainer ModularUIGuiContainer = new ModularUIGuiContainer(ui, entityPlayer.containerMenu.containerId);

            minecraft.setScreen(ModularUIGuiContainer);
            entityPlayer.containerMenu = ModularUIGuiContainer.getMenu();
        });
    }

    public void openGui(WidgetGroup panel) {

        RenderSystem.recordRenderCall(() -> {
            final var minecraft = Minecraft.getInstance();
            final var entityPlayer = minecraft.player;

            ModularUI ui = new ModularUI(panel, IUIHolder.EMPTY, entityPlayer);
            ui.setFullScreen();
            ui.initWidgets();
            ModularUIGuiContainer ModularUIGuiContainer = new ModularUIGuiContainer(ui, entityPlayer.containerMenu.containerId);

            minecraft.setScreen(ModularUIGuiContainer);
            entityPlayer.containerMenu = ModularUIGuiContainer.getMenu();
        });
    }
}
