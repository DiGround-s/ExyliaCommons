package net.exylia.commons.item;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de un item interactivo almacenada en memoria
 * Esta clase contiene toda la configuración que NO necesita persistir en NBT
 */
public class ItemConfiguration {

    // Propiedades visuales
    private final String material;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final boolean glowing;
    private final boolean hideAttributes;

    // Comportamiento
    private final List<String> commands;
    private final String action;
    private final boolean consumeOnUse;
    private final boolean cancelEvent;
    private final boolean stackable;

    // Sistema de usos
    private final int maxUses;
    private final String usesDisplayFormat;
    private final boolean showUsesInLore;
    private final boolean showUsesInName;

    // Placeholders
    private final boolean usePlaceholders;

    private ItemConfiguration(Builder builder) {
        this.material = builder.material;
        this.name = builder.name;
        this.lore = new ArrayList<>(builder.lore);
        this.amount = builder.amount;
        this.glowing = builder.glowing;
        this.hideAttributes = builder.hideAttributes;
        this.commands = new ArrayList<>(builder.commands);
        this.action = builder.action;
        this.consumeOnUse = builder.consumeOnUse;
        this.cancelEvent = builder.cancelEvent;
        this.stackable = builder.stackable;
        this.maxUses = builder.maxUses;
        this.usesDisplayFormat = builder.usesDisplayFormat;
        this.showUsesInLore = builder.showUsesInLore;
        this.showUsesInName = builder.showUsesInName;
        this.usePlaceholders = builder.usePlaceholders;
    }

    // ===== GETTERS =====

    public String getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return new ArrayList<>(lore); }
    public int getAmount() { return amount; }
    public boolean isGlowing() { return glowing; }
    public boolean shouldHideAttributes() { return hideAttributes; }
    public List<String> getCommands() { return new ArrayList<>(commands); }
    public String getAction() { return action; }
    public boolean shouldConsumeOnUse() { return consumeOnUse; }
    public boolean shouldCancelEvent() { return cancelEvent; }
    public boolean isStackable() { return stackable; }
    public int getMaxUses() { return maxUses; }
    public String getUsesDisplayFormat() { return usesDisplayFormat; }
    public boolean shouldShowUsesInLore() { return showUsesInLore; }
    public boolean shouldShowUsesInName() { return showUsesInName; }
    public boolean usesPlaceholders() { return usePlaceholders; }

    // ===== BUILDER =====

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromConfig(ConfigurationSection config) {
        return new Builder().loadFromConfig(config);
    }

    public static class Builder {
        private String material = "STONE";
        private String name = null;
        private List<String> lore = new ArrayList<>();
        private int amount = 1;
        private boolean glowing = false;
        private boolean hideAttributes = true;
        private List<String> commands = new ArrayList<>();
        private String action = null;
        private boolean consumeOnUse = false;
        private boolean cancelEvent = true;
        private boolean stackable = true;
        private int maxUses = -1;
        private String usesDisplayFormat = "§7Usos: §f%current%§7/§f%max%";
        private boolean showUsesInLore = true;
        private boolean showUsesInName = false;
        private boolean usePlaceholders = false;

        public Builder material(String material) {
            this.material = material;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lore(List<String> lore) {
            this.lore = new ArrayList<>(lore);
            return this;
        }

        public Builder lore(String... lore) {
            this.lore = List.of(lore);
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder glowing(boolean glowing) {
            this.glowing = glowing;
            return this;
        }

        public Builder hideAttributes(boolean hideAttributes) {
            this.hideAttributes = hideAttributes;
            return this;
        }

        public Builder commands(List<String> commands) {
            this.commands = new ArrayList<>(commands);
            return this;
        }

        public Builder commands(String... commands) {
            this.commands = List.of(commands);
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder consumeOnUse(boolean consumeOnUse) {
            this.consumeOnUse = consumeOnUse;
            return this;
        }

        public Builder cancelEvent(boolean cancelEvent) {
            this.cancelEvent = cancelEvent;
            return this;
        }

        public Builder stackable(boolean stackable) {
            this.stackable = stackable;
            return this;
        }

        public Builder maxUses(int maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        public Builder usesDisplayFormat(String format) {
            this.usesDisplayFormat = format;
            return this;
        }

        public Builder showUsesInLore(boolean show) {
            this.showUsesInLore = show;
            return this;
        }

        public Builder showUsesInName(boolean show) {
            this.showUsesInName = show;
            return this;
        }

        public Builder usePlaceholders(boolean use) {
            this.usePlaceholders = use;
            return this;
        }

        public Builder loadFromConfig(ConfigurationSection config) {
            if (config.contains("material")) {
                material(config.getString("material"));
            }

            if (config.contains("name")) {
                name(config.getString("name"));
            }

            if (config.contains("lore")) {
                if (config.isList("lore")) {
                    lore(config.getStringList("lore"));
                } else {
                    lore(config.getString("lore"));
                }
            }

            if (config.contains("amount")) {
                amount(config.getInt("amount"));
            }

            if (config.contains("glow") || config.contains("glowing")) {
                glowing(config.getBoolean("glow", config.getBoolean("glowing")));
            }

            if (config.contains("hide-attributes")) {
                hideAttributes(config.getBoolean("hide-attributes"));
            }

            if (config.contains("commands")) {
                if (config.isList("commands")) {
                    commands(config.getStringList("commands"));
                } else {
                    commands(config.getString("commands"));
                }
            }

            if (config.contains("action")) {
                action(config.getString("action"));
            }

            if (config.contains("consume-on-use")) {
                consumeOnUse(config.getBoolean("consume-on-use"));
            }

            if (config.contains("cancel-event")) {
                cancelEvent(config.getBoolean("cancel-event"));
            }

            if (config.contains("stackable")) {
                stackable(config.getBoolean("stackable"));
            }

            if (config.contains("max-uses")) {
                maxUses(config.getInt("max-uses"));
            }

            if (config.contains("uses-format")) {
                usesDisplayFormat(config.getString("uses-format"));
            }

            if (config.contains("show-uses-in-lore")) {
                showUsesInLore(config.getBoolean("show-uses-in-lore"));
            }

            if (config.contains("show-uses-in-name")) {
                showUsesInName(config.getBoolean("show-uses-in-name"));
            }

            // Auto-detectar placeholders
            boolean autoDetectPlaceholders = false;
            String nameText = config.getString("name", "");
            List<String> loreList = config.getStringList("lore");

            if (containsPlaceholders(nameText) ||
                    loreList.stream().anyMatch(this::containsPlaceholders)) {
                autoDetectPlaceholders = true;
            }

            usePlaceholders(config.getBoolean("use-placeholders", autoDetectPlaceholders));

            return this;
        }

        private boolean containsPlaceholders(String text) {
            return text != null && (text.contains("%") || text.contains("{") || text.contains("<"));
        }

        public ItemConfiguration build() {
            return new ItemConfiguration(this);
        }
    }

    @Override
    public String toString() {
        return "ItemConfiguration{" +
                "material='" + material + '\'' +
                ", name='" + name + '\'' +
                ", commands=" + commands.size() +
                ", action='" + action + '\'' +
                ", maxUses=" + maxUses +
                ", stackable=" + stackable +
                '}';
    }
}