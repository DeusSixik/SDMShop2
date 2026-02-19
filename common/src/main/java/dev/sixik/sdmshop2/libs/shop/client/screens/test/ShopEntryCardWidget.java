package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import net.minecraft.client.Minecraft;

public class ShopEntryCardWidget extends WidgetGroup {

    public ShopEntryCardWidget(int width, int height) {
        super(0, 0, width, height);

        // --- 1. Фон и обводка карточки ---
        // Обычное состояние
        ColorRectAndBorderTexture normalTexture = new ColorRectAndBorderTexture(ShopColors.BG_CARD, ShopColors.BORDER, 1f).setRadius(8);
        setBackground(normalTexture);

        // Состояние при наведении (Меняем цвет рамки на Accent)
        ColorRectAndBorderTexture hoverTexture = new ColorRectAndBorderTexture(ShopColors.BG_CARD, ShopColors.ACCENT, 1f).setRadius(8);
        setHoverTexture(hoverTexture);
        // ВАЖНО: говорим LDLib не рисовать обычный фон, когда мы навели мышку (чтобы не было двойного рендера)
        setDrawBackgroundWhenHover(false);

        // --- 2. Заголовок ---
        LabelWidget title = new LabelWidget(12, 12, "Эпический Сет").setTextColor(ShopColors.TEXT_MAIN);
        addWidget(title);

        // --- 3. Превью предметов (Мини-инвентарь) ---
        buildItemPreview(12, 35);

        // --- 4. Кнопки оплаты (Prices) ---
        buildPriceButtons(12, height - 40, width - 24); // Размещаем в самом низу
    }

    private void buildItemPreview(int startX, int startY) {
        // В LDLib должен быть виджет для рендера ItemStack.
        // Здесь мы имитируем его простым квадратом (item-slot из CSS)

        WidgetGroup slot1 = new WidgetGroup(startX, startY, 24, 24);
        slot1.setBackground(new ColorRectAndBorderTexture(0x66000000, 0xFF444444, 1).setRadius(4));
        addWidget(slot1);

        WidgetGroup slot2 = new WidgetGroup(startX + 30, startY, 24, 24);
        slot2.setBackground(new ColorRectAndBorderTexture(0x66000000, 0xFF444444, 1).setRadius(4));
        addWidget(slot2);

        // Текст "+ еще 2"
        LabelWidget moreTxt = new LabelWidget(startX + 60, startY + 8, "+ ещё 2").setTextColor(ShopColors.TEXT_MUTED);
        addWidget(moreTxt);
    }

    private void buildPriceButtons(int startX, int startY, int btnWidth) {

        // --- Вариант 1: Оплата алмазами ---

        // Обертка для первой кнопки
        WidgetGroup buyBtn1Group = new WidgetGroup(startX, startY - 20, btnWidth, 16);

        // Сама кнопка
        ButtonWidget buyBtn1 = new ButtonWidget(0, 0, btnWidth, 16, cd -> {
            System.out.println("Куплено за Алмазы!");
        });
        buyBtn1.setBackground(new ColorRectTexture(ShopColors.PRICE_BG).setRadius(6));
        buyBtn1.setHoverTexture(new ColorRectTexture(ShopColors.ACCENT).setRadius(6));
        buyBtn1.setDrawBackgroundWhenHover(false);

        buyBtn1Group.addWidget(buyBtn1);

        // Текст поверх кнопки
        buyBtn1Group.addWidget(new LabelWidget(6, 4, "Купить").setTextColor(ShopColors.TEXT_MAIN));
        buyBtn1Group.addWidget(new LabelWidget(btnWidth - 40, 4, "💎 15").setTextColor(ShopColors.TEXT_MAIN));

        addWidget(buyBtn1Group); // Добавляем обертку в карточку товара


        // --- Вариант 2: Смешанная оплата (Рубли + Золото) ---

        WidgetGroup buyBtn2Group = new WidgetGroup(startX, startY, btnWidth, 16);

        ButtonWidget buyBtn2 = new ButtonWidget(0, 0, btnWidth, 16, cd -> {
            System.out.println("Куплено за Рубли и Золото!");
        });
        buyBtn2.setBackground(new ColorRectAndBorderTexture(ShopColors.PRICE_BG, ShopColors.ACCENT_ORANGE, 1).setRadius(6));
        buyBtn2.setHoverTexture(new ColorRectTexture(ShopColors.ACCENT_ORANGE).setRadius(6));
        buyBtn2.setDrawBackgroundWhenHover(false);

        buyBtn2Group.addWidget(buyBtn2);
        buyBtn2Group.addWidget(new LabelWidget(6, 4, "Купить").setTextColor(ShopColors.TEXT_MAIN));

        String complexPriceText = "₽ 20 + 🟡 2";
        int textWidth = Minecraft.getInstance().font.width(complexPriceText);
        buyBtn2Group.addWidget(new LabelWidget(btnWidth - textWidth - 6, 4, complexPriceText).setTextColor(ShopColors.TEXT_MAIN));

        addWidget(buyBtn2Group);
    }
}