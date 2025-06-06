package net.exylia.commons.menu;

import net.exylia.commons.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builder para items de cooldown/valores numéricos
 */
public class NumericItemBuilder {
    private String name;
    private String material;
    private String unit;
    private int minValue = -1;
    private int maxValue = Integer.MAX_VALUE;
    private int[] increments = {1, 10}; // normal, shift
    private String colorPrimary = "§e";
    private String colorSecondary = "§7";
    private String colorSuccess = "§a";
    private Consumer<Integer> setter;
    private Supplier<Integer> getter;
    private Player player;
    private Function<Integer, String> displayFormatter;
    private Runnable onUpdate;

    public NumericItemBuilder(String name, String material, String unit) {
        this.name = name;
        this.material = material;
        this.unit = unit;
        this.displayFormatter = value -> value == -1 ? "Disabled" : value + " " + unit;
    }

    public NumericItemBuilder range(int min, int max) {
        this.minValue = min;
        this.maxValue = max;
        return this;
    }

    public NumericItemBuilder increments(int normal, int shift) {
        this.increments = new int[]{normal, shift};
        return this;
    }

    public NumericItemBuilder colors(String primary, String secondary, String success) {
        this.colorPrimary = primary;
        this.colorSecondary = secondary;
        this.colorSuccess = success;
        return this;
    }

    public NumericItemBuilder setter(Consumer<Integer> setter) {
        this.setter = setter;
        return this;
    }

    public NumericItemBuilder getter(Supplier<Integer> getter) {
        this.getter = getter;
        return this;
    }

    public NumericItemBuilder player(Player player) {
        this.player = player;
        return this;
    }

    public NumericItemBuilder displayFormatter(Function<Integer, String> formatter) {
        this.displayFormatter = formatter;
        return this;
    }

    public NumericItemBuilder onUpdate(Runnable callback) {
        this.onUpdate = callback;
        return this;
    }

    public MenuItem build() {
        MenuItem item = new MenuItem(material);
        updateItemState(item);

        item.setClickHandler(clickInfo -> {
            int currentValue = getter.get();
            int newValue = calculateNewValue(currentValue, clickInfo.clickType());

            setter.accept(newValue);
            updateItemState(item);

            if (player != null) {
                MessageUtils.sendMessageAsync(player, colorSuccess + name + " set to " +
                        displayFormatter.apply(newValue) + "!");
            }

            if (onUpdate != null) {
                onUpdate.run();
            }
        });

        return item;
    }

    private void updateItemState(MenuItem item) {
        int currentValue = getter.get();
        item.setName(colorPrimary + name);
        item.setLore(
                "Current: " + colorSecondary + displayFormatter.apply(currentValue),
                "",
                colorSecondary + "Left-click: +" + increments[0] + " " + unit,
                colorSecondary + "Right-click: -" + increments[0] + " " + unit,
                colorSecondary + "Shift+Left-click: +" + increments[1] + " " + unit,
                colorSecondary + "Shift+Right-click: -" + increments[1] + " " + unit,
                colorSecondary + "Middle-click: Reset to " + (minValue == -1 ? "disabled" : "default")
        );
    }

    private int calculateNewValue(int currentValue, ClickType clickType) {
        int newValue = currentValue;

        switch (clickType) {
            case MIDDLE:
                newValue = minValue;
                break;
            case LEFT:
                newValue = (currentValue == -1) ? increments[0] : currentValue + increments[0];
                break;
            case RIGHT:
                newValue = Math.max(minValue, (currentValue == -1) ? minValue : currentValue - increments[0]);
                break;
            case SHIFT_LEFT:
                newValue = (currentValue == -1) ? increments[1] : currentValue + increments[1];
                break;
            case SHIFT_RIGHT:
                newValue = Math.max(minValue, (currentValue == -1) ? minValue : currentValue - increments[1]);
                break;
        }

        return Math.min(maxValue, Math.max(minValue, newValue));
    }
}