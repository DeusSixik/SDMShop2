package dev.sixik.sdmshop2.libs.shop.client.screens.ui;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.MarqueeScrollWidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.client.textures.GradientTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public class ShopEntryCard extends WidgetGroup {

    private int w;

    private int h;
    private LabelWidget title;
    private DraggableScrollableWidgetGroup preview;
    private GradientTexture progresTexture= new GradientTexture(0xFF08215C,0xFF9156B8,true);

    public ShopEntryCard(int width, int height) {
        super(0, 0, width, height);

        w = width;
        h = height;
        // --- 1. Фон и обводка карточки ---
        // Обычное состояние
        ColorRectAndBorderTexture normalTexture = new ColorRectAndBorderTexture(ShopColors.BG_CARD, ShopColors.BORDER, 1f).setRadius(8);
        setBackground(normalTexture);

        // Состояние при наведении (Меняем цвет рамки на Accent)
        ColorRectAndBorderTexture hoverTexture = new ColorRectAndBorderTexture(ShopColors.ACCENT, ShopColors.BORDER, 1f).setRadius(8);
        setHoverTexture(hoverTexture);
        // ВАЖНО: говорим LDLib не рисовать обычный фон, когда мы навели мышку (чтобы не было двойного рендера)
        setDrawBackgroundWhenHover(false);

        // --- 2. Заголовок ---
        title = new LabelWidget(0, 0, "Эпический Сет").setTextColor(ShopColors.TEXT_MAIN);
        addWidget(title);

        // --- 3. Превью предметов (Мини-инвентарь) ---

        preview = new MarqueeScrollWidgetGroup(5, title.getSizeHeight() +10, w-10, h/5, 0.5f);
        preview.setBackground(new ColorRectAndBorderTexture(0x66000000, 0xFF444444, 1).setRadius(4));
        preview.setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.HORIZONTAL);
        buildItemPreview();
        addWidget(preview);

//        // --- 4. Лимитер (Prices) ---
        ProgressWidget limiter = new ProgressWidget(() -> 0.5,5, preview.getPositionY() + preview.getSizeHeight() + 10, w-10, 16);
        limiter.setProgressTexture(new ColorRectTexture(0xFF444444).setRadius(4),progresTexture);
        addWidget(limiter);

//        // --- 5. Кнопки оплаты (Prices) ---
        buildPriceButtons(12, height - 40, width - 24); // Размещаем в самом низу
    }

    @Override
    public void initWidget() {
        super.initWidget();
        title.setSelfPosition((w - title.getSizeWidth())/2,3);
    }

    private void buildItemPreview() {

        for (int i = 0; i<10; i++){
            Widget item = new WidgetGroup();
            item.setSize(preview.getSizeHeight() - 4,preview.getSizeHeight() - 4);
            item.setSelfPosition(0+(preview.getSizeHeight() - 2)*i,2);
            item.setBackground(new ItemStackTexture(Items.ACACIA_WOOD));
            preview.addWidget(item);
        }

//        WidgetGroup slot2 = new WidgetGroup(startX + 30, startY, 24, 24);
//        slot2.setBackground(new ColorRectAndBorderTexture(0x66000000, 0xFF444444, 1).setRadius(4));
//        addWidget(slot2);
//
//        // Текст "+ еще 2"
//        LabelWidget moreTxt = new LabelWidget(startX + 60, startY + 8, "+ ещё 2").setTextColor(ShopColors.TEXT_MUTED);
//        addWidget(moreTxt);
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
        buyBtn2.setBackground(new ColorRectTexture(ShopColors.BG_ORANGE).setRadius(6));
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