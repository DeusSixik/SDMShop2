package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements;

import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.base.ShopWidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class ShopScreen extends ShopWidgetGroup {

    public static ShopScreen Instance;

    @Getter
    private final Minecraft minecraft;
    @Getter
    private final Window window;

    @Getter
    private ShopTabsPanel tabsPanel;

    @Getter
    private ObjectArrayList<CatalogComponent> catalogComponents;

    private final Map<String, State> states = new Object2ObjectOpenHashMap<>();

    public ShopScreen() {
        this.minecraft = Minecraft.getInstance();
        this.window = minecraft.getWindow();
        Instance = this;
    }

    @Override
    public void initWidget() {
        setBackground(new ColorRectAndBorderTexture());

        this.catalogComponents = SDMShopClient.Shop.getCategories().getCatalogsComponents();

        alightWidget();
        addWidget(tabsPanel = new ShopTabsPanel(this));
        customInitWidget();
    }

    @Override
    public void alightWidget() {
        setSize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        super.alightWidget();
    }

    protected void alightWidgets() {
       /*
            cur_* = Current N
            tsw_*  = TabsWidget
        */

        final Size cur_size = getSize();
        final int cur_w = cur_size.width;
        final int cur_h = cur_size.height;


        final int tsw_w = cur_w / 4;
        final int tsw_h_offset = (cur_h / 4);
        final int tsw_h = cur_h - tsw_h_offset * 2;
        tabsPanel.setSize(tsw_w, tsw_h);

        final int tsw_x = 0;
        final int tsw_y = tsw_h_offset;
        tabsPanel.setSelfPosition(tsw_x, tsw_y);
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        alightWidget();
    }

    @Nullable
    public State getState(String id) {
        return states.get(id);
    }

    public void putState(String id, State state) {
        states.put(id, state);
    }

    public void updateState(String id, Object value) {
        State state = getState(id);
        if(state == null) return;
        state.setValue(value);
    }

    public void updateState(String id, State.Type type, Object defaultValue) {
        State state = getState(id);
        if(state == null) return;
        if(state.type != type)
            throw new IllegalArgumentException("Can't update state because '" + type.name() + " != " + state.type.name() + "' !");
        state.update(type, defaultValue);
    }

    public static class State {

        @Getter
        private Type type;

        @Getter
        private Object defaultValue;

        @Setter
        private @Nullable Object value;

        public State(Type type, Object defaultValue) {
            this(type, defaultValue, null);
        }

        public State(Type type, Object defaultValue, @Nullable Object value) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        public Object getValue() {
            return value == null ? defaultValue : value;
        }

        void update(Type type, Object defaultValue) {
            update(type, defaultValue, null);
        }

        void update(Type type, Object defaultValue, @Nullable Object value) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        public enum Type {
            String,
            Int,
            Bool,
            Double,
            Float,
            ShopEntity
        }
    }
}
