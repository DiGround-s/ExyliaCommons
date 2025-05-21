package net.exylia.commons.menu;

import net.exylia.commons.utils.AdapterFactory;
import net.exylia.commons.utils.ColorUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.exylia.commons.utils.ItemMetaAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
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

/**
 * Representa un ítem interactivo en un menú
 */
public class MenuItem {
    private ItemStack itemStack;
    private final ItemMetaAdapter adapter = AdapterFactory.getItemMetaAdapter();
    private Consumer<MenuClickInfo> clickHandler;
    private String menuItemId;
    private String rawName;
    private List<String> rawLore;
    private boolean usePlaceholders = false;
    private boolean dynamicUpdate = false;
    private long updateInterval = 20L; // 1 segundo por defecto
    private Player placeholderPlayer = null; // Jugador específico para procesar los placeholders
    private List<String> commands = new ArrayList<>(); // Lista de comandos a ejecutar

    /**
     * Constructor del ítem de menú
     * @param material Material del ítem
     */
    public MenuItem(Material material) {
        this.itemStack = new ItemStack(material);
        this.menuItemId = UUID.randomUUID().toString();
    }

    /**
     * Constructor del ítem de menú
     * @param itemStack ItemStack para usar directamente
     */
    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.menuItemId = UUID.randomUUID().toString();
    }

    /**
     * Establece el nombre del ítem
     * @param name Nombre (admite códigos de color y placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setName(String name) {
        this.rawName = name;
        ItemMeta meta = itemStack.getItemMeta();
        adapter.setDisplayName(meta, ColorUtils.parse(name));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece el nombre del ítem directamente con un componente
     * @param name Componente de nombre ya formateado
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setName(Component name) {
        ItemMeta meta = itemStack.getItemMeta();
        adapter.setDisplayName(meta, name);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece la descripción del ítem
     * @param lore Líneas de descripción (admiten códigos de color y placeholders)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setLore(String... lore) {
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
    public MenuItem setLoreFromList(List<String> lore) {
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
     * Establece la descripción del ítem directamente con componentes
     * @param lore Lista de componentes de descripción ya formateados
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setLore(List<Component> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        adapter.setLore(meta, lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Establece la acción al hacer clic en el ítem
     * @param clickHandler Manejador del clic
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setClickHandler(Consumer<MenuClickInfo> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    /**
     * Establece la cantidad del ítem
     * @param amount Cantidad (1-64)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setAmount(int amount) {
        itemStack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    /**
     * Añade un brillo al ítem (encantamiento oculto)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setGlowing(boolean glowing) {
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
     * Añade flags al ítem
     * @param flags Flags a añadir
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem addItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Oculta todos los atributos del ítem
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem hideAllAttributes() {
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
    public MenuItem setNBTTag(JavaPlugin plugin, String key, String value) {
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
     * Establece el ID único del ítem en el menú
     * @param id ID personalizado
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setId(String id) {
        this.menuItemId = id;
        return this;
    }

    /**
     * Obtiene el ID único del ítem en el menú
     * @return ID del ítem
     */
    public String getId() {
        return menuItemId;
    }

    /**
     * Obtiene el manejador de clics
     * @return Manejador de clics
     */
    public Consumer<MenuClickInfo> getClickHandler() {
        return clickHandler;
    }

    /**
     * Obtiene el ItemStack asociado
     * @return ItemStack del ítem
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Clona el ítem
     * @return Copia del ítem
     */
    public MenuItem clone() {
        MenuItem clone = new MenuItem(this.itemStack.clone());
        clone.clickHandler = this.clickHandler;
        clone.menuItemId = this.menuItemId;
        clone.rawName = this.rawName;
        if (this.rawLore != null) {
            clone.rawLore = new ArrayList<>(this.rawLore);
        }
        clone.usePlaceholders = this.usePlaceholders;
        clone.dynamicUpdate = this.dynamicUpdate;
        clone.updateInterval = this.updateInterval;
        clone.placeholderPlayer = this.placeholderPlayer;
        clone.commands = new ArrayList<>(this.commands);
        return clone;
    }

    public void applySkullOwner(Player player) {
        if (this.itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) this.itemStack.getItemMeta();
            meta.setOwningPlayer(player);
            this.itemStack.setItemMeta(meta);
        }
    }

    /**
     * Activa el uso de placeholders en el nombre y lore del ítem
     * @param use true para activar placeholders, false para desactivarlos
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem usePlaceholders(boolean use) {
        this.usePlaceholders = use;
        return this;
    }

    /**
     * Activa la actualización dinámica del ítem
     * @param update true para activar actualización, false para desactivarla
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setDynamicUpdate(boolean update) {
        this.dynamicUpdate = update;
        return this;
    }

    /**
     * Establece el intervalo de actualización del ítem
     * @param ticks Intervalo en ticks
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setUpdateInterval(long ticks) {
        this.updateInterval = Math.max(1, ticks);
        return this;
    }

    /**
     * Establece un jugador específico para procesar los placeholders
     * @param player Jugador para procesar los placeholders (o null para usar el jugador que ve el menú)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setPlaceholderPlayer(Player player) {
        this.placeholderPlayer = player;
        return this;
    }

    /**
     * Obtiene el jugador específico para procesar los placeholders
     * @return Jugador específico o null si no hay configurado
     */
    public Player getPlaceholderPlayer() {
        return placeholderPlayer;
    }

    // En MenuItem.java, añadir un campo para el objeto de contexto
    private Object placeholderContext = null;

    /**
     * Establece un objeto de contexto para procesar placeholders personalizados
     * @param context Objeto de contexto (ej: Player target)
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setPlaceholderContext(Object context) {
        this.placeholderContext = context;
        return this;
    }

    /**
     * Obtiene el objeto de contexto para placeholders personalizados
     * @return Objeto de contexto o null si no hay configurado
     */
    public Object getPlaceholderContext() {
        return placeholderContext;
    }

    /**
     * Añade un comando a ejecutar cuando se hace clic en el ítem
     * @param command Comando a ejecutar (formato: "player: /comando" o "console: /comando")
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem addCommand(String command) {
        this.commands.add(command);
        return this;
    }

    /**
     * Establece la lista de comandos a ejecutar
     * @param commands Lista de comandos (formato: "player: /comando" o "console: /comando")
     * @return El mismo ítem (para encadenamiento)
     */
    public MenuItem setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
        return this;
    }

    /**
     * Obtiene la lista de comandos a ejecutar
     * @return Lista de comandos
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Ejecuta los comandos configurados
     * @param player Jugador que hace clic en el ítem
     */
    public void executeCommands(Player player) {
        if (commands.isEmpty()) return;

        for (String cmd : commands) {
            String processedCmd = cmd;

            if (placeholderContext != null) {
                processedCmd = CustomPlaceholderManager.process(processedCmd, placeholderContext);
            }

            if (MenuManager.isPlaceholderAPIEnabled()) {
                processedCmd = PlaceholderAPI.setPlaceholders(
                        placeholderPlayer != null ? placeholderPlayer : player,
                        processedCmd
                );
            }

            if (processedCmd.startsWith("player: ")) {
                String playerCmd = processedCmd.substring(8).trim();
                player.performCommand(playerCmd);
            } else if (processedCmd.startsWith("console: ")) {
                String consoleCmd = processedCmd.substring(9).trim();
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(console, consoleCmd);
            }
        }
    }

    /**
     * Actualiza los placeholders del ítem para un jugador específico
     *
     * @param player Jugador para procesar los placeholders (si no hay un placeholderPlayer configurado)
     */
    public void updatePlaceholders(Player player) {
        if (!usePlaceholders) return;

        Player targetPlayer = (placeholderPlayer != null) ? placeholderPlayer : player;

        ItemMeta meta = itemStack.getItemMeta();

        if (rawName != null && !rawName.isEmpty()) {
            String processedName = rawName;
            if (placeholderContext != null) {
                processedName = CustomPlaceholderManager.process(processedName, placeholderContext);
            }
            if (MenuManager.isPlaceholderAPIEnabled()) {
                processedName = PlaceholderAPI.setPlaceholders(targetPlayer, processedName);
            }
            adapter.setDisplayName(meta, ColorUtils.parse(processedName));
        }

        if (rawLore != null && !rawLore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : rawLore) {
                String processedLine = line;
                if (placeholderContext != null) {
                    processedLine = CustomPlaceholderManager.process(processedLine, placeholderContext);
                }
                if (MenuManager.isPlaceholderAPIEnabled()) {
                    processedLine = PlaceholderAPI.setPlaceholders(targetPlayer, processedLine);
                }
                loreComponents.add(ColorUtils.parse(processedLine));
            }
            adapter.setLore(meta, loreComponents);
        }

        itemStack.setItemMeta(meta);
    }

    /**
     * Comprueba si el ítem usa placeholders
     * @return true si usa placeholders
     */
    public boolean usesPlaceholders() {
        return usePlaceholders;
    }

    /**
     * Comprueba si el ítem debe actualizarse dinámicamente
     * @return true si debe actualizarse
     */
    public boolean needsDynamicUpdate() {
        return dynamicUpdate;
    }

    /**
     * Obtiene el intervalo de actualización
     * @return Intervalo en ticks
     */
    public long getUpdateInterval() {
        return updateInterval;
    }
}