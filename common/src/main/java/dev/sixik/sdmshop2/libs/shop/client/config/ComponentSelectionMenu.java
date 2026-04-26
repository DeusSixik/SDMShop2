package dev.sixik.sdmshop2.libs.shop.client.config;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.effects.DiscountComponent;

import java.util.List;
import java.util.function.Consumer;

public class ComponentSelectionMenu {

    public static DialogWidget showComponentSelector(WidgetGroup parent, String title, Consumer<ShopComponent> onSelected) {
        // 1. Создаем диалог. Флаг "true" означает, что это клиентский виджет.
        // Он автоматически добавится в parent.
        DialogWidget dialog = new DialogWidget(parent, true);

        // Включаем закрытие по клику вне окна и скрытие заднего фона (как в NEI/JEI)
        dialog.setClickClose(true);
        dialog.setParentInVisible(); // Прячет основной UI, пока открыто окно

        // 2. Создаем красивое окошко по центру через встроенный метод LDLib
        // Он сам отрисует шапку, фон и отцентрирует окно.
        WidgetGroup container = DialogWidget.createContainer(dialog, 200, 250, title);
        var size = container.getSize();

        // 3. Создаем скролл-панель для списка (учитываем высоту шапки 15px из createContainer)
        DraggableScrollableWidgetGroup scroll = new DraggableScrollableWidgetGroup(5, 5, size.width - 10, size.height - 35);
        scroll.setYScrollBarWidth(4);
        scroll.setYBarStyle(null, ColorPattern.WHITE.rectTexture().setRadius(2));

        WidgetGroup listContainer = new WidgetGroup(0, 0, size.width - 15, 0);
        listContainer.setLayout(Layout.VERTICAL_LEFT);
        listContainer.setDynamicSized(true);

        // 4. Заполняем список компонентами
        for (ShopComponent comp : getAllAvailableComponents()) {
            // Используем встроенный генератор красивых кнопок из DialogWidget
            DialogWidget.createButton(listContainer, 0, 0, size.width - 15, 20, comp.getType().getId().toString(), () -> {
                dialog.close(); // Встроенный метод закрытия с возвратом видимости фону
                if (onSelected != null) onSelected.accept(comp);
            });
        }

        scroll.addWidget(listContainer);
        container.addWidget(scroll);

        // 5. Кнопка "Отмена" в самом низу окна
        DialogWidget.createButton(container, (size.width - 60) / 2, size.height - 20, 60, 15, "ldlib.gui.tips.cancel", () -> {
            dialog.close();
            if (onSelected != null) onSelected.accept(null);
        });

        return dialog;
    }

    // Заглушка для получения списка
    private static Iterable<ShopComponent> getAllAvailableComponents() {
        // Здесь будет обращение к твоему реестру SDM Shop
        return List.of(new DiscountComponent(), new LimiterComponent());
    }
}
