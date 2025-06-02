package net.exylia.commons.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {

    private final JavaPlugin plugin;

    /**
     * Constructor del MenuBuilder
     * @param plugin Plugin principal
     */
    public MenuBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Crea un menú a partir de la configuración
     * @param menuConfig Configuración del menú (FileConfiguration)
     * @param player Jugador para el que se crea el menú
     * @return Menú creado o null si no existe la configuración
     */
    public Menu buildMenu(FileConfiguration menuConfig, Player player) {
        return buildMenuFromSection(menuConfig, player);
    }

    /**
     * Crea un menú a partir de una sección de configuración
     * @param menuSection Sección de configuración del menú
     * @param player Jugador para el que se crea el menú
     * @return Menú creado o null si no existe la configuración
     */
    public Menu buildMenu(ConfigurationSection menuSection, Player player) {
        return buildMenuFromSection(menuSection, player);
    }

    /**
     * Crea un menú a partir de la configuración con soporte para placeholders personalizados
     * @param menuConfig Configuración del menú (FileConfiguration)
     * @param player Jugador para el que se crea el menú
     * @param placeholderContext Objeto de contexto para placeholders personalizados
     * @return Menú creado o null si no existe la configuración
     */
    public Menu buildMenu(FileConfiguration menuConfig, Player player, Object placeholderContext) {
        return buildMenuFromSection(menuConfig, player, placeholderContext);
    }

    /**
     * Crea un menú a partir de una sección de configuración con soporte para placeholders personalizados
     * @param menuSection Sección de configuración del menú
     * @param player Jugador para el que se crea el menú
     * @param placeholderContext Objeto de contexto para placeholders personalizados
     * @return Menú creado o null si no existe la configuración
     */
    public Menu buildMenu(ConfigurationSection menuSection, Player player, Object placeholderContext) {
        return buildMenuFromSection(menuSection, player, placeholderContext);
    }

    /**
     * Crea un menú paginado a partir de la configuración
     * @param menuConfig Configuración del menú (FileConfiguration)
     * @param player Jugador para el que se crea el menú
     * @param itemSlots Posiciones donde colocar los ítems paginados
     * @return Menú paginado creado o null si no existe la configuración
     */
    public PaginationMenu buildPaginationMenu(FileConfiguration menuConfig, Player player, int... itemSlots) {
        return buildPaginationMenuFromSection(menuConfig, player, itemSlots);
    }

    /**
     * Crea un menú paginado a partir de una sección de configuración
     * @param menuSection Sección de configuración del menú
     * @param player Jugador para el que se crea el menú
     * @param itemSlots Posiciones donde colocar los ítems paginados
     * @return Menú paginado creado o null si no existe la configuración
     */
    public PaginationMenu buildPaginationMenu(ConfigurationSection menuSection, Player player, int... itemSlots) {
        return buildPaginationMenuFromSection(menuSection, player, itemSlots);
    }

    /**
     * Implementación interna para crear un menú desde cualquier tipo de configuración
     */
    private Menu buildMenuFromSection(ConfigurationSection menuSection, Player player) {
        return buildMenuFromSection(menuSection, player, null);
    }

    /**
     * Implementación interna para crear un menú desde cualquier tipo de configuración con placeholders
     */
    private Menu buildMenuFromSection(ConfigurationSection menuSection, Player player, Object placeholderContext) {
        // Propiedades básicas del menú
        String title = menuSection.getString("title", "Menu");
        int rows = menuSection.getInt("rows", 3);
        Menu menu = new Menu(title, rows);

        // Configuración de actualizaciones dinámicas del menú
        if (menuSection.getBoolean("dynamic_updates", false)) {
            long updateInterval = menuSection.getLong("update_interval", 20L);
            menu.enableDynamicUpdates(plugin, updateInterval);
        }

        // Configurar placeholders en el título si está especificado en la configuración
        if (placeholderContext != null && menuSection.getBoolean("use_placeholders_in_title", false)) {
            menu.usePlaceholdersInTitle(true);
            menu.setTitlePlaceholderContext(placeholderContext);
        }

        // Cargar ítems
        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                if (itemSection != null) {
                    MenuItem menuItem = placeholderContext != null
                            ? buildMenuItem(itemSection, player, placeholderContext)
                            : buildMenuItem(itemSection, player);

                    // Obtener los slots para este ítem
                    List<Integer> slots = getItemSlots(itemSection, rows);

                    // Colocar el ítem en todos los slots especificados
                    for (int slot : slots) {
                        if (slot >= 0 && slot < rows * 9) {
                            menu.setItem(slot, menuItem);
                        }
                    }
                }
            }
        }

        return menu;
    }

    /**
     * Implementación interna para crear un menú paginado desde cualquier tipo de configuración
     */
    private PaginationMenu buildPaginationMenuFromSection(ConfigurationSection menuSection, Player player, int... itemSlots) {
        String title = menuSection.getString("title", "Menu");
        int rows = menuSection.getInt("rows", 6);

        PaginationMenu paginationMenu = new PaginationMenu(title, rows, itemSlots);

        ConfigurationSection prevSection = menuSection.getConfigurationSection("prev_button");
        if (prevSection != null) {
            MenuItem prevButton = buildMenuItem(prevSection, player);

            List<Integer> prevSlots = getItemSlots(prevSection, rows);
            int prevSlot = prevSlots.isEmpty() ? (rows * 9 - 9) : prevSlots.get(0);

            paginationMenu.setPreviousPageButton(prevButton, prevSlot);
        }

        ConfigurationSection nextSection = menuSection.getConfigurationSection("next_button");
        if (nextSection != null) {
            MenuItem nextButton = buildMenuItem(nextSection, player);

            List<Integer> nextSlots = getItemSlots(nextSection, rows);
            int nextSlot = nextSlots.isEmpty() ? (rows * 9 - 1) : nextSlots.get(0);

            paginationMenu.setNextPageButton(nextButton, nextSlot);
        }

        ConfigurationSection fillerSection = menuSection.getConfigurationSection("filler");
        if (fillerSection != null) {
            MenuItem fillerItem = buildMenuItem(fillerSection, player);
            paginationMenu.setFillerItem(fillerItem);
        }

        return paginationMenu;
    }

    /**
     * Obtiene todos los slots para un ítem según su configuración
     * @param itemSection Sección de configuración del ítem
     * @param rows Número de filas del menú para validación
     * @return Lista de slots donde debe colocarse el ítem
     */
    private List<Integer> getItemSlots(ConfigurationSection itemSection, int rows) {
        List<Integer> slots = new ArrayList<>();
        int maxSlot = rows * 9 - 1;

        if (itemSection.contains("slot")) {
            int slot = itemSection.getInt("slot", -1);
            if (slot >= 0 && slot <= maxSlot) {
                slots.add(slot);
            }
        }

        if (itemSection.contains("slots") && itemSection.isString("slots")) {
            String slotsString = itemSection.getString("slots");
            if (slotsString != null) {
                String[] parts = slotsString.split(",");

                for (String part : parts) {
                    part = part.trim();

                    if (part.contains("-")) {
                        String[] range = part.split("-");
                        if (range.length == 2) {
                            try {
                                int start = Integer.parseInt(range[0].trim());
                                int end = Integer.parseInt(range[1].trim());

                                start = Math.max(0, start);
                                end = Math.min(maxSlot, end);

                                for (int i = start; i <= end; i++) {
                                    slots.add(i);
                                }
                            } catch (NumberFormatException e) {
                                // Ignorar
                            }
                        }
                    }
                    else {
                        try {
                            int slot = Integer.parseInt(part);
                            if (slot >= 0 && slot <= maxSlot) {
                                slots.add(slot);
                            }
                        } catch (NumberFormatException e) {
                            // Ignorar
                        }
                    }
                }
            }
        }

        // "slots: [0,1,2,3]"
        if (itemSection.contains("slots") && itemSection.isList("slots")) {
            List<Integer> slotsList = itemSection.getIntegerList("slots");
            for (int slot : slotsList) {
                if (slot >= 0 && slot <= maxSlot) {
                    slots.add(slot);
                }
            }
        }

        return slots;
    }

    /**
     * Crea un ítem a partir de la configuración
     * @param itemSection Sección de configuración del ítem
     * @param player Jugador para el que se crea el ítem
     * @return Ítem creado
     */
    public static MenuItem buildMenuItem(ConfigurationSection itemSection, Player player) {
        MenuItem menuItem = new MenuItem(itemSection.getString("material", "STONE"), player);

        // Propiedades básicas
        if (itemSection.contains("name")) {
            menuItem.setName(itemSection.getString("name"));
        }

        if (itemSection.contains("amount")) {
            menuItem.setAmount(itemSection.getInt("amount", 1));
        }

        if (itemSection.contains("glowing")) {
            menuItem.setGlowing(itemSection.getBoolean("glowing", false));
        }

        // Lore
        if (itemSection.contains("lore")) {
            menuItem.setLore(itemSection.getStringList("lore").toArray(new String[0]));
        }

        // Ocultar atributos
        if (itemSection.getBoolean("hide_attributes", false)) {
            menuItem.hideAllAttributes();
        }

        // Soporte para PlaceholderAPI
        if (itemSection.getBoolean("use_placeholders", false)) {
            menuItem.usePlaceholders(true);
            menuItem.setPlaceholderPlayer(player);
        }

        // Actualización dinámica
        if (itemSection.getBoolean("dynamic_update", false)) {
            menuItem.setDynamicUpdate(true);
            if (itemSection.contains("update_interval")) {
                menuItem.setUpdateInterval(itemSection.getLong("update_interval", 20L));
            }
        }

        // Comandos
        if (itemSection.contains("commands")) {
            List<String> commands = itemSection.getStringList("commands");
            if (!commands.isEmpty()) {
                menuItem.setCommands(commands);
            }
        }

        return menuItem;
    }

    /**
     * Crea un ítem a partir de la configuración con contexto de placeholders
     * @param itemSection Sección de configuración del ítem
     * @param player Jugador para el que se crea el ítem
     * @param placeholderContext Objeto de contexto para placeholders personalizados
     * @return Ítem creado
     */
    private MenuItem buildMenuItem(ConfigurationSection itemSection, Player player, Object placeholderContext) {
        MenuItem menuItem = buildMenuItem(itemSection, player);

        // Si el ítem usa placeholders, establecer el contexto
        if (menuItem.usesPlaceholders() && placeholderContext != null) {
            menuItem.setPlaceholderContext(placeholderContext);
        }

        return menuItem;
    }
}