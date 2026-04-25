package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.network.chat.Component;

public class ExternTextFieldWidget extends TextFieldWidget {

    public ExternTextFieldWidget() {

    }

    public void setTextFieldHeight(int h) {
        ((EditBoxAccessor)this.textField).setHeight(h);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    public TextFieldWidget setUuidOnly() {
        setMaxStringLength(36); // Жесткий лимит UUID
        setValidator(s -> {
            if (s == null || s.isEmpty()) return s;
            /*
                Разрешаем вводить только цифры, буквы A-F и дефисы
             */
            if (s.matches("^[0-9a-fA-F\\-]*$")) {
                return s;
            }
            /*
                Блокируем всё остальное (например, буквы G-Z)
             */
            return this.currentString;
        });
        return this;
    }

    @RemapForJS("setNumbersOnlyLong")
    public TextFieldWidget setNumbersOnly(long minValue, long maxValue) {
        setValidator(s -> {
            /*
                Разрешаем промежуточные состояния для ввода
             */
            if (s == null || s.isEmpty() || s.equals("-")) return s;
            try {
                long value = Long.parseLong(s);
                if (minValue <= value && value <= maxValue) return s;
                if (value < minValue) return String.valueOf(minValue);
                return String.valueOf(maxValue);
            } catch (NumberFormatException ignored) { }
            return this.currentString; // Блокируем буквы
        });

        if (minValue == Long.MIN_VALUE && maxValue == Long.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.3");
        } else if (minValue == Long.MIN_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.2", maxValue);
        } else if (maxValue == Long.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.1", minValue);
        } else {
            hover = Component.translatable("ldlib.gui.text_field.number.0", minValue, maxValue);
        }
        return setWheelDur(1);
    }

    @RemapForJS("setNumbersOnlyInt")
    public TextFieldWidget setNumbersOnly(int minValue, int maxValue) {
        setValidator(s -> {
            /*
                Разрешаем промежуточные состояния для ввода
             */
            if (s == null || s.isEmpty() || s.equals("-")) return s;
            try {
                int value = Integer.parseInt(s);
                if (minValue <= value && value <= maxValue) return s;
                if (value < minValue) return String.valueOf(minValue);
                return String.valueOf(maxValue);
            } catch (NumberFormatException ignored) { }
            return this.currentString;
        });

        if (minValue == Integer.MIN_VALUE && maxValue == Integer.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.3");
        } else if (minValue == Integer.MIN_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.2", maxValue);
        } else if (maxValue == Integer.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.1", minValue);
        } else {
            hover = Component.translatable("ldlib.gui.text_field.number.0", minValue, maxValue);
        }
        return setWheelDur(1);
    }

    @RemapForJS("setNumbersOnlyFloat")
    public TextFieldWidget setNumbersOnly(float minValue, float maxValue) {
        setValidator(s -> {
            /*
                Разрешаем промежуточные состояния для ввода
                Для дробей разрешаем еще и точку
             */
            if (s == null || s.isEmpty() || s.equals("-") || s.equals(".") || s.equals("-.")) return s;
            try {
                float value = Float.parseFloat(s);
                if (minValue <= value && value <= maxValue) return s;
                if (value < minValue) return String.valueOf(minValue);
                return String.valueOf(maxValue);
            } catch (NumberFormatException ignored) { }
            return this.currentString;
        });

        if (minValue == -Float.MAX_VALUE && maxValue == Float.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.3");
        } else if (minValue == -Float.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.2", maxValue);
        } else if (maxValue == Float.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.1", minValue);
        } else {
            hover = Component.translatable("ldlib.gui.text_field.number.0", minValue, maxValue);
        }
        return setWheelDur(0.1f);
    }

    @RemapForJS("setNumbersOnlyDouble")
    public TextFieldWidget setNumbersOnly(double minValue, double maxValue) {
        setValidator(s -> {
            /*
                Разрешаем промежуточные состояния для ввода
             */
            if (s == null || s.isEmpty() || s.equals("-") || s.equals(".") || s.equals("-.")) return s;
            try {
                double value = Double.parseDouble(s);
                if (minValue <= value && value <= maxValue) return s;
                if (value < minValue) return String.valueOf(minValue);
                return String.valueOf(maxValue);
            } catch (NumberFormatException ignored) { }
            return this.currentString;
        });

        if (minValue == -Double.MAX_VALUE && maxValue == Double.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.3");
        } else if (minValue == -Double.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.2", maxValue);
        } else if (maxValue == Double.MAX_VALUE) {
            hover = Component.translatable("ldlib.gui.text_field.number.1", minValue);
        } else {
            hover = Component.translatable("ldlib.gui.text_field.number.0", minValue, maxValue);
        }
        return setWheelDur(0.1f);
    }

    public TextFieldWidget setResourceLocationOnly() {
        setValidator(s -> {
            if (s == null || s.isEmpty()) return s;

            /*
                Авто-исправление: в ResourceLocation не может быть заглавных букв и пробелов
             */
            String fixed = s.toLowerCase().replace(' ', '_');

            /*
                Проверяем на наличие запрещенных символов (разрешены 0-9, a-z, _, -, :, /)
             */
            if (fixed.matches("^[a-z0-9_\\-\\:\\/]*$")) {
                return fixed;
            }

            /*
                Блокируем некорректный символ
             */
            return this.currentString;
        });

        this.hover = Component.translatable("ldlib.gui.text_field.resourcelocation");
        return this;
    }
}
