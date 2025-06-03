package net.exylia.commons.item;

import net.exylia.commons.actions.ActionContext;
import net.exylia.commons.actions.ActionSource;
import net.exylia.commons.actions.GlobalActionManager;
import net.exylia.commons.command.CommandExecutor;
import net.exylia.commons.menu.CustomPlaceholderManager;
import net.exylia.commons.utils.AdapterFactory;
import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.ItemMetaAdapter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static net.exylia.commons.ExyliaPlugin.isPlaceholderAPIEnabled;
import static net.exylia.commons.utils.DebugUtils.logWarn;
import static net.exylia.commons.utils.SkullUtils.*;

/**
 * Representa un ítem interactivo que puede ejecutar acciones
 */
public class InteractiveItem {

    private final ItemStack itemStack;
    private final ItemMetaAdapter adapter = AdapterFactory.getItemMetaAdapter();
    private Consumer<ItemClickInfo> clickHandler;
    private String itemId;
    private String rawName;
    private List<String> rawLore;
    private String rawMaterialString;
    private Player materialPlaceholderPlayer;
    private boolean usePlaceholders = false;
    private Player placeholderPlayer = null;
    private List<String> commands = new ArrayList<>();
    private Object placeholderContext = null;
    private String action = null;
    private boolean consumeOnUse = false;
    private boolean cancelEvent = true;
    private int maxUses = -1; // -1 = usos infinitos
    private int currentUses = -1; // -1 = usos infinitos
    private boolean stackable = true;
    private String usesDisplayFormat = "§7Usos: §f%current%§7/§f%max%";
    private boolean showUsesInLore = true;
    private boolean showUsesInName = false;

    /**
     * Constructor del ítem interactivo usando String
     * @param materialString String que representa el material o tipo de cabeza
     */
    public InteractiveItem(String materialString) {
        this.rawMaterialString = materialString;
        this.itemStack = createItemFromString(materialString);
        this.itemId = UUID.randomUUID().toString();
    }

    /**
     * Constructor del ítem interactivo usando String con jugador específico para placeholders
     * @param materialString String que representa el material o tipo de cabeza (puede contener placeholders)
     * @param player Jugador para procesar los placeholders del material
     */
    public InteractiveItem(String materialString, Player player) {
        this.rawMaterialString = materialString;
        this.materialPlaceholderPlayer = player;
        this.itemStack = createItemFromString(processPlaceholdersInMaterial(materialString, player));
        this.itemId = UUID.randomUUID().toString();
    }

    /**
     * Constructor del ítem interactivo
     * @param itemStack ItemStack para usar directamente
     */
    public InteractiveItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemId = UUID.randomUUID().toString();
    }

    /**
     * Procesa los placeholders en el string del material
     * @param materialString String del material con placeholders
     * @param player Jugador para procesar los placeholders
     * @return String del material con placeholders procesados
     */
    private String processPlaceholdersInMaterial(String materialString, Player player) {
        if (materialString == null || player == null) {
            return materialString;
        }

        String processed = materialString;

        // Procesar placeholders personalizados si hay contexto
        if (placeholderContext != null) {
            processed = CustomPlaceholderManager.process(processed, placeholderContext);
        }

        // Procesar PlaceholderAPI si está disponible
        if (isPlaceholderAPIEnabled()) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        return processed;
    }

    /**
     * Crea un ItemStack basado en un string
     * @param materialString String que puede ser un material o cabeza personalizada
     * @return ItemStack creado
     */
    private ItemStack createItemFromString(String materialString) {
        if (materialString == null || materialString.isEmpty()) {
            logWarn("Material string is null or empty, using STONE");
            return new ItemStack(Material.STONE);
        }

        if (materialString.startsWith("headbase-")) {
            String base64 = materialString.substring(9);
            return createHeadFromBase64(base64);
        }

        if (materialString.startsWith("headurl-")) {
            String url = materialString.substring(8);
            return createHeadFromUrl(url);
        }

        if (materialString.startsWith("playerhead-")) {
            String playerName = materialString.substring(11);
            return createPlayerHead(playerName);
        }

        try {
            Material material = Material.valueOf(materialString.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            logWarn("Invalid material: " + materialString + ", using STONE");
            return new ItemStack(Material.STONE);
        }
    }

    /**
     * Establece el nombre del ítem
     * @param name Nombre (admite códigos de color y placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setName(String name) {
        this.rawName = name;
        ItemMeta meta = itemStack.getItemMeta();
        adapter.setDisplayName(meta, ColorUtils.parse(name));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece la descripción del ítem
     * @param lore Líneas de descripción (admiten códigos de color y placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setLore(String... lore) {
        this.rawLore = Arrays.asList(lore);

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(ColorUtils.parse(line));
        }

        ItemMeta meta = itemStack.getItemMeta();
        adapter.setLore(meta, loreComponents);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece la descripción del ítem usando una lista de strings
     * @param lore Lista de líneas de descripción (admiten códigos de color y placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setLoreFromList(List<String> lore) {
        this.rawLore = new ArrayList<>(lore);

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(ColorUtils.parse(line));
        }

        ItemMeta meta = itemStack.getItemMeta();
        adapter.setLore(meta, loreComponents);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece la acción al hacer clic en el ítem
     * @param clickHandler Manejador del clic
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setClickHandler(Consumer<ItemClickInfo> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    /**
     * Establece la cantidad del ítem
     * @param amount Cantidad (1-64)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setAmount(int amount) {
        itemStack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    /**
     * Añade un brillo al ítem (encantamiento oculto)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setGlowing(boolean glowing) {
        ItemMeta meta = itemStack.getItemMeta();

        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.DURABILITY);
        }

        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Oculta todos los atributos del ítem
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem hideAllAttributes() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_DYE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_PLACED_ON,
                ItemFlag.HIDE_POTION_EFFECTS,
                ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Añade un identificador personalizado al ítem
     * @param plugin Plugin para crear la clave
     * @param key Nombre de la clave
     * @param value Valor a guardar
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setNBTTag(JavaPlugin plugin, String key, String value) {
        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Obtiene un identificador personalizado del ítem
     * @param plugin Plugin para crear la clave
     * @param key Nombre de la clave
     * @return Valor guardado o null si no existe
     */
    public String getNBTTag(JavaPlugin plugin, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * Establece el ID único del ítem
     * @param id ID personalizado
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setId(String id) {
        this.itemId = id;
        return this;
    }

    /**
     * Obtiene el ID único del ítem
     * @return ID del ítem
     */
    public String getId() {
        return itemId;
    }

    /**
     * Establece una acción personalizada para ejecutar al hacer clic
     * @param action String de acción (ej: "show_help", "open_shop gold", "teleport spawn")
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Obtiene la acción configurada
     * @return String de acción o null si no hay configurada
     */
    public String getAction() {
        return action;
    }

    /**
     * Verifica si el ítem tiene una acción configurada
     * @return true si tiene acción
     */
    public boolean hasAction() {
        return action != null && !action.trim().isEmpty();
    }

    /**
     * Establece el número máximo de usos del ítem
     * @param maxUses Número máximo de usos (-1 para infinitos)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        this.currentUses = maxUses;
        updateUsesDisplay();
        return this;
    }

    /**
     * Obtiene el número máximo de usos
     * @return Número máximo de usos (-1 si es infinito)
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * Obtiene los usos actuales restantes
     * @return Usos restantes (-1 si es infinito)
     */
    public int getCurrentUses() {
        return currentUses;
    }

    /**
     * Establece los usos actuales del ítem
     * @param uses Usos actuales
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setCurrentUses(int uses) {
        this.currentUses = uses;
        updateUsesDisplay();
        return this;
    }

    /**
     * Verifica si el ítem tiene usos limitados
     * @return true si tiene usos limitados
     */
    public boolean hasLimitedUses() {
        return maxUses > 0;
    }

    /**
     * Verifica si el ítem aún tiene usos disponibles
     * @return true si tiene usos disponibles
     */
    public boolean hasUsesRemaining() {
        return maxUses == -1 || currentUses > 0;
    }

    /**
     * Consume un uso del ítem
     * @return true si se consumió un uso, false si no tenía usos restantes
     */
    public boolean consumeUse() {
        if (maxUses == -1) return true; // Usos infinitos

        if (currentUses > 0) {
            currentUses--;
            updateUsesDisplay();
            return true;
        }
        return false;
    }

    /**
     * Establece si el ítem puede apilarse con otros ítems similares
     * @param stackable true si puede apilarse
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setStackable(boolean stackable) {
        this.stackable = stackable;
        updateStackability();
        return this;
    }

    /**
     * Verifica si el ítem puede apilarse
     * @return true si puede apilarse
     */
    public boolean isStackable() {
        return stackable;
    }

    /**
     * Establece el formato de visualización de usos
     * @param format Formato (usa %current% y %max% como placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setUsesDisplayFormat(String format) {
        this.usesDisplayFormat = format;
        updateUsesDisplay();
        return this;
    }

    /**
     * Establece si mostrar los usos en el lore
     * @param show true para mostrar en el lore
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setShowUsesInLore(boolean show) {
        this.showUsesInLore = show;
        updateUsesDisplay();
        return this;
    }

    /**
     * Establece si mostrar los usos en el nombre
     * @param show true para mostrar en el nombre
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setShowUsesInName(boolean show) {
        this.showUsesInName = show;
        updateUsesDisplay();
        return this;
    }

    /**
     * Actualiza la visualización de usos en el ítem
     */
    private void updateUsesDisplay() {
        if (!hasLimitedUses()) return;

        String usesText = usesDisplayFormat
                .replace("%current%", String.valueOf(currentUses))
                .replace("%max%", String.valueOf(maxUses));

        ItemMeta meta = itemStack.getItemMeta();

        // Actualizar nombre si está habilitado
        if (showUsesInName && rawName != null) {
            String displayName = rawName;
            if (!rawName.contains("%uses%")) {
                displayName = rawName + " " + usesText;
            } else {
                displayName = rawName.replace("%uses%", usesText);
            }
            adapter.setDisplayName(meta, ColorUtils.parse(displayName));
        }

        // Actualizar lore si está habilitado
        if (showUsesInLore) {
            List<Component> loreComponents = new ArrayList<>();

            // Añadir lore original si existe
            if (rawLore != null) {
                for (String line : rawLore) {
                    loreComponents.add(ColorUtils.parse(line));
                }
            }

            // Añadir línea de usos si no está ya incluida en el lore original
            boolean usesAlreadyInLore = rawLore != null && rawLore.stream()
                    .anyMatch(line -> line.contains("%uses%"));

            if (!usesAlreadyInLore) {
                loreComponents.add(ColorUtils.parse(""));
                loreComponents.add(ColorUtils.parse(usesText));
            } else if (rawLore != null) {
                // Reemplazar %uses% en el lore original
                loreComponents.clear();
                for (String line : rawLore) {
                    String processedLine = line.replace("%uses%", usesText);
                    loreComponents.add(ColorUtils.parse(processedLine));
                }
            }

            adapter.setLore(meta, loreComponents);
        }

        itemStack.setItemMeta(meta);
    }

    /**
     * Actualiza la capacidad de apilamiento del ítem
     */
    private void updateStackability() {
        ItemMeta meta = itemStack.getItemMeta();

        if (!stackable) {
            // Hacer único añadiendo un NBT único
            NamespacedKey uniqueKey = new NamespacedKey(
                    ItemManager.getPlugin() != null ? ItemManager.getPlugin() : JavaPlugin.getProvidingPlugin(InteractiveItem.class),
                    "unique_id"
            );
            meta.getPersistentDataContainer().set(uniqueKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        }

        itemStack.setItemMeta(meta);
    }

    /**
     * Establece si se debe cancelar el evento de interacción
     * @param cancel true para cancelar el evento
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setCancelEvent(boolean cancel) {
        this.cancelEvent = cancel;
        return this;
    }

    /**
     * Añade un comando a ejecutar cuando se hace clic en el ítem
     * @param command Comando a ejecutar (formato: "player: /comando" o "console: /comando")
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem addCommand(String command) {
        this.commands.add(command);
        return this;
    }

    /**
     * Establece la lista de comandos a ejecutar
     * @param commands Lista de comandos (formato: "player: /comando" o "console: /comando")
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
        return this;
    }

    /**
     * Activa el uso de placeholders en el nombre y lore del ítem
     * @param use true para activar placeholders, false para desactivarlos
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem usePlaceholders(boolean use) {
        this.usePlaceholders = use;
        return this;
    }

    /**
     * Establece un jugador específico para procesar los placeholders
     * @param player Jugador para procesar los placeholders (o null para usar el jugador que usa el ítem)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setPlaceholderPlayer(Player player) {
        this.placeholderPlayer = player;
        return this;
    }

    /**
     * Establece un objeto de contexto para procesar placeholders personalizados
     * @param context Objeto de contexto (ej: Player target)
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setPlaceholderContext(Object context) {
        this.placeholderContext = context;
        return this;
    }

    /**
     * Ejecuta la acción configurada si existe
     * @param clickInfo Información del clic
     * @return true si se ejecutó una acción, false si no
     */
    public boolean executeAction(ItemClickInfo clickInfo) {
        if (hasAction()) {
            ActionContext context = new ActionContext(clickInfo.player(), clickInfo.source())
                    .withData("clickType", clickInfo.clickType())
                    .withData("slot", clickInfo.slot())
                    .withData("item", this)
                    .withData("itemStack", clickInfo.itemStack());

            return GlobalActionManager.executeAction(action, context);
        }
        return false;
    }

    /**
     * Ejecuta los comandos configurados
     * @param player Jugador que hace clic en el ítem
     */
    public void executeCommands(Player player) {
        CommandExecutor.builder(player)
                .withPlaceholderPlayer(placeholderPlayer)
                .withPlaceholderContext(placeholderContext)
                .execute(commands);
    }

    /**
     * Establece si el ítem debe consumirse al usar
     * @param consume true para consumir al usar
     * @return El mismo ítem (para encadenamiento)
     */
    public InteractiveItem setConsumeOnUse(boolean consume) {
        this.consumeOnUse = consume;
        return this;
    }

    /**
     * Actualiza los placeholders del ítem para un jugador específico
     * @param player Jugador para procesar los placeholders (si no hay un placeholderPlayer configurado)
     */
    public void updatePlaceholders(Player player) {
        if (!usePlaceholders) return;

        Player targetPlayer = (placeholderPlayer != null) ? placeholderPlayer : player;

        ItemMeta meta = itemStack.getItemMeta();

        if (rawName != null && !rawName.isEmpty()) {
            String processedName = rawName;

            // Procesar placeholder de usos si está presente
            if (hasLimitedUses() && processedName.contains("%uses%")) {
                String usesText = usesDisplayFormat
                        .replace("%current%", String.valueOf(currentUses))
                        .replace("%max%", String.valueOf(maxUses));
                processedName = processedName.replace("%uses%", usesText);
            }

            if (placeholderContext != null) {
                processedName = CustomPlaceholderManager.process(processedName, placeholderContext);
            }
            if (isPlaceholderAPIEnabled()) {
                processedName = PlaceholderAPI.setPlaceholders(targetPlayer, processedName);
            }
            adapter.setDisplayName(meta, ColorUtils.parse(processedName));
        }

        if (rawLore != null && !rawLore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : rawLore) {
                String processedLine = line;

                // Procesar placeholder de usos si está presente
                if (hasLimitedUses() && processedLine.contains("%uses%")) {
                    String usesText = usesDisplayFormat
                            .replace("%current%", String.valueOf(currentUses))
                            .replace("%max%", String.valueOf(maxUses));
                    processedLine = processedLine.replace("%uses%", usesText);
                }

                if (placeholderContext != null) {
                    processedLine = CustomPlaceholderManager.process(processedLine, placeholderContext);
                }
                if (isPlaceholderAPIEnabled()) {
                    processedLine = PlaceholderAPI.setPlaceholders(targetPlayer, processedLine);
                }
                loreComponents.add(ColorUtils.parse(processedLine));
            }

            // Añadir usos al final del lore si está habilitado y no está en el lore original
            if (showUsesInLore && hasLimitedUses() &&
                    (rawLore.stream().noneMatch(line -> line.contains("%uses%")))) {
                loreComponents.add(ColorUtils.parse(""));
                String usesText = usesDisplayFormat
                        .replace("%current%", String.valueOf(currentUses))
                        .replace("%max%", String.valueOf(maxUses));
                loreComponents.add(ColorUtils.parse(usesText));
            }

            adapter.setLore(meta, loreComponents);
        }

        itemStack.setItemMeta(meta);
    }

    /**
     * Obtiene el ItemStack asociado
     * @return ItemStack del ítem
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Obtiene el manejador de clics
     * @return Manejador de clics
     */
    public Consumer<ItemClickInfo> getClickHandler() {
        return clickHandler;
    }

    /**
     * Verifica si el ítem debe consumirse al usar
     * @return true si debe consumirse
     */
    public boolean shouldConsumeOnUse() {
        return consumeOnUse;
    }

    /**
     * Verifica si debe cancelar el evento
     * @return true si debe cancelar
     */
    public boolean shouldCancelEvent() {
        return cancelEvent;
    }

    /**
     * Obtiene la lista de comandos a ejecutar
     * @return Lista de comandos
     */
    public List<String> getCommands() {
        return commands;
    }

    @Override
    public InteractiveItem clone() {
        InteractiveItem clone = new InteractiveItem(this.itemStack.clone());
        clone.clickHandler = this.clickHandler;
        clone.itemId = this.itemId;
        clone.rawName = this.rawName;
        clone.rawMaterialString = this.rawMaterialString;
        clone.materialPlaceholderPlayer = this.materialPlaceholderPlayer;
        if (this.rawLore != null) {
            clone.rawLore = new ArrayList<>(this.rawLore);
        }
        clone.usePlaceholders = this.usePlaceholders;
        clone.placeholderPlayer = this.placeholderPlayer;
        clone.commands = new ArrayList<>(this.commands);
        clone.placeholderContext = this.placeholderContext;
        clone.action = this.action;
        clone.consumeOnUse = this.consumeOnUse;
        clone.cancelEvent = this.cancelEvent;
        clone.maxUses = this.maxUses;
        clone.currentUses = this.currentUses;
        clone.stackable = this.stackable;
        clone.usesDisplayFormat = this.usesDisplayFormat;
        clone.showUsesInLore = this.showUsesInLore;
        clone.showUsesInName = this.showUsesInName;
        return clone;
    }
}