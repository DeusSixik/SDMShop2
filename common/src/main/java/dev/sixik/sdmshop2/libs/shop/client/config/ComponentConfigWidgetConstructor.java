package dev.sixik.sdmshop2.libs.shop.client.config;

import com.lowdragmc.lowdraglib.gui.widget.*;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.CollapsedGroupWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ExternTextFieldWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMBlockSelectorWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMItemStackSelectorWidget;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ComponentConfigWidgetConstructor {

    private static int DEFAULT_W = 60;
    private static int DEFAULT_H = 15;

    public static void createShopOfferWidget(WidgetGroup root, ShopOffer offer, int w) {
        final var components = offer.getComponents();

        for (int i = 0; i < components.size(); i++) {
            ShopComponent component = components.get(i);

            CollapsedGroupWidget widget = new CollapsedGroupWidget(Component.literal(component.getType().getId().toString()), w);
            widget.addWidget(new ComponentConfigurationWidget(component));
            root.addWidget(widget);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Widget createWidget(Object targetComponent, ComponentConfigAccess.CachedField cachedField) {

        Widget someWidget = createField(targetComponent, cachedField);
        if(someWidget != null) return someWidget;

        someWidget = createSwitch(targetComponent, cachedField);
        if(someWidget != null) return someWidget;

        if (cachedField.type().isEnum()) {
            Object[] constants = cachedField.type().getEnumConstants();

            SelectorWidget widget = new SelectorWidget();
            List<String> options = Arrays.stream(constants)
                    .map(obj -> ((Enum<?>) obj).name())
                    .toList();
            widget.setCandidates(options);

            try {
                Object currentValue = cachedField.getter().invoke(targetComponent);
                if (currentValue != null) {
                    widget.setValue(((Enum<?>) currentValue).name());
                }
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get Enum value", e);
            }

            widget.setOnChanged(selectedString -> {
                try {
                    Enum<?> newValue = Enum.valueOf((Class<Enum>) cachedField.type(), selectedString);
                    cachedField.setter().invoke(targetComponent, newValue);
                } catch (IllegalArgumentException e) {
                    SDMShop2.LOGGER.error("Unknown Enum value: {}", selectedString);
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set Enum value", e);
                }
            });

            return widget;
        }

        if(cachedField.type() == ItemStack.class) {
            SDMItemStackSelectorWidget selectorWidget = new SDMItemStackSelectorWidget(0, 0, DEFAULT_W + 22, false);
            try {
                Object val = cachedField.getter().invoke(targetComponent);
                selectorWidget.setItemStack(val != null ? (ItemStack) val : ItemStack.EMPTY);
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get ItemStack value", e);
            }
            selectorWidget.setOnItemStackUpdate(itemStack -> {
                try {
                    cachedField.setter().invoke(targetComponent, itemStack);
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set ItemStack value", e);
                }
            });
            return selectorWidget;
        } else if(cachedField.type() == Item.class) {
            SDMItemStackSelectorWidget selectorWidget = new SDMItemStackSelectorWidget(0, 0, DEFAULT_W + 22, false);
            try {
                Object val = cachedField.getter().invoke(targetComponent);
                selectorWidget.setItemStack(val != null ? ((Item) val).getDefaultInstance() : ItemStack.EMPTY);
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get Item value", e);
            }
            selectorWidget.setOnItemStackUpdate(itemStack -> {
                try {
                    cachedField.setter().invoke(targetComponent, itemStack.getItem());
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set Item value", e);
                }
            });
            return selectorWidget;
        }

        if(cachedField.type() == Block.class) {
            SDMBlockSelectorWidget widget = new SDMBlockSelectorWidget(0, 0, DEFAULT_W + 22, false);
            widget.setOnBlockStateUpdate(((blockState) -> {
                try {
                    cachedField.setter().invoke(targetComponent, blockState.getBlock());
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set Block value", e);
                }
            }));

            try {
                Object val = cachedField.getter().invoke(targetComponent);
                widget.setBlock(val != null ? ((Block) val).defaultBlockState() : Blocks.AIR.defaultBlockState());
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get Block value", e);
            }

            return widget;
        } else if(cachedField.type() == BlockState.class) {
            SDMBlockSelectorWidget widget = new SDMBlockSelectorWidget(0, 0, DEFAULT_W + 22, false);
            widget.setOnBlockStateUpdate(((blockState) -> {
                try {
                    cachedField.setter().invoke(targetComponent, blockState);
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set BlockState value", e);
                }
            }));

            try {
                Object val = cachedField.getter().invoke(targetComponent);
                widget.setBlock(val != null ? ((BlockState) val) : Blocks.AIR.defaultBlockState());
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get BlockState value", e);
            }

            return widget;
        }

        return null;
    }

    private static @Nullable SwitchWidget createSwitch(Object targetComponent, ComponentConfigAccess.CachedField cachedField) {
        SwitchWidget widget = new SwitchWidget();
        if(cachedField.type() == boolean.class || cachedField.type() == Boolean.class) {
            try {
                widget.setPressed(cachedField.getter().invoke(targetComponent) == Boolean.TRUE);
            } catch (Throwable e) {
                SDMShop2.LOGGER.error("Failed to get boolean value", e);
                widget.setPressed(false);
            }
            widget.setOnPressCallback((s1, s2) -> {
                try {
                    cachedField.setter().invoke(targetComponent, s2);
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to toggle boolean value", e);
                }
            });
        } else
            return null;

        return widget;
    }

    private static @Nullable ExternTextFieldWidget createField(Object targetComponent, ComponentConfigAccess.CachedField cachedField) {
        ExternTextFieldWidget widget = new ExternTextFieldWidget();
        Class<?> type = cachedField.type();

        if(type == ResourceLocation.class) {
            widget.setResourceLocationOnly();
            widget.setTextSupplier(() -> {
                try {
                    Object val = cachedField.getter().invoke(targetComponent);
                    return val != null ? val.toString() : "";
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to get ResourceLocation value", e);
                    return "";
                }
            });
            widget.setTextResponder(text -> {
                if (text == null || text.isEmpty()) return;

                ResourceLocation loc = text.contains(":")
                        ? ResourceLocation.tryParse(text)
                        : ResourceLocation.tryBuild("minecraft", text);

                if (loc != null) {
                    try {
                        cachedField.setter().invoke(targetComponent, loc);
                    } catch (Throwable e) {
                        SDMShop2.LOGGER.error("Failed to set ResourceLocation value", e);
                    }
                }
            });
        } else if(type == UUID.class) {
            widget.setUuidOnly();
            widget.setTextSupplier(() -> {
                try {
                    Object val = cachedField.getter().invoke(targetComponent);
                    return val != null ? val.toString() : "";
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to get UUID value", e);
                    return "";
                }
            });
            widget.setTextResponder(text -> {
                try {
                    cachedField.setter().invoke(targetComponent, UUID.fromString(text));
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set UUID value", e);
                }
            });
        } else if (type == String.class) {
            widget.setTextSupplier(() -> {
                try {
                    Object val = cachedField.getter().invoke(targetComponent);
                    return val != null ? (String) val : "";
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to get String value", e);
                    return "";
                }
            });
            widget.setTextResponder(text -> {
                try {
                    cachedField.setter().invoke(targetComponent, text);
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set String value", e);
                }
            });
        }
        else if (type == int.class || type == Integer.class) {
            widget.setNumbersOnly(Integer.MIN_VALUE, Integer.MAX_VALUE);
            widget.setTextSupplier(() -> {
                try {
                    return String.valueOf(cachedField.getter().invoke(targetComponent));
                } catch (Throwable e) {
                    return "0";
                }
            });
            widget.setTextResponder(text -> {
                if (text.isEmpty() || text.equals("-")) return;
                try {
                    cachedField.setter().invoke(targetComponent, Integer.parseInt(text));
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set int value", e);
                }
            });
        }
        else if (type == long.class || type == Long.class) {
            widget.setNumbersOnly(Long.MIN_VALUE, Long.MAX_VALUE);
            widget.setTextSupplier(() -> {
                try {
                    return String.valueOf(cachedField.getter().invoke(targetComponent));
                } catch (Throwable e) {
                    return "0";
                }
            });
            widget.setTextResponder(text -> {
                if (text.isEmpty() || text.equals("-")) return;
                try {
                    cachedField.setter().invoke(targetComponent, Long.parseLong(text));
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set long value", e);
                }
            });
        }
        else if (type == double.class || type == Double.class) {
            widget.setNumbersOnly(-Double.MAX_VALUE, Double.MAX_VALUE);
            widget.setTextSupplier(() -> {
                try {
                    return String.valueOf(cachedField.getter().invoke(targetComponent));
                } catch (Throwable e) {
                    return "0.0";
                }
            });
            widget.setTextResponder(text -> {
                if (text.isEmpty() || text.equals("-") || text.equals(".")) return;
                try {
                    cachedField.setter().invoke(targetComponent, Double.parseDouble(text));
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set double value", e);
                }
            });
        }
        else if (type == float.class || type == Float.class) {
            widget.setNumbersOnly(-Float.MAX_VALUE, Float.MAX_VALUE);
            widget.setTextSupplier(() -> {
                try {
                    return String.valueOf(cachedField.getter().invoke(targetComponent));
                } catch (Throwable e) {
                    return "0.0";
                }
            });
            widget.setTextResponder(text -> {
                if (text.isEmpty() || text.equals("-") || text.equals(".")) return;
                try {
                    cachedField.setter().invoke(targetComponent, Float.parseFloat(text));
                } catch (Throwable e) {
                    SDMShop2.LOGGER.error("Failed to set float value", e);
                }
            });
        }
        else {
            return null;
        }

        try {
            widget.setCurrentString(String.valueOf(cachedField.getter().invoke(targetComponent)));
        } catch (Throwable e) {
            widget.setCurrentString("0.0");
        }
        return widget;
    }
}
