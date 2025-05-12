package net.exylia.commons.menu;

import net.exylia.commons.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructor de menús a partir de archivos de configuración
 */
public class MenuBuilder {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    /**
     * Constructor del MenuBuilder
     * @param plugin Plugin principal
     * @param configManager Gestor de configuraciones
     */
    public MenuBuilder(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Crea un menú a partir de la configuración
     * @param menuId Identificador del menú en la configuración
     * @return Menú creado o null si no existe la configuración
     */
    public Menu buildMenu(String menuId) {
        FileConfiguration menuConfig = configManager.getConfig("menus");
        if (menuConfig == null) {
            return null;
        }

        ConfigurationSection menuSection = menuConfig.getConfigurationSection("menus." + menuId);
        if (menuSection == null) {
            return null;
        }

        // Propiedades básicas del menú
        String title = menuSection.getString("title", "Menu");
        int rows = menuSection.getInt("rows", 3);
        Menu menu = new Menu(title, rows);

        // Configuración de actualizaciones dinámicas del menú
        if (menuSection.getBoolean("dynamic_updates", false)) {
            long updateInterval = menuSection.getLong("update_interval", 20L);
            menu.enableDynamicUpdates(plugin, updateInterval);
        }

        // Cargar ítems
        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                if (itemSection != null) {
                    MenuItem menuItem = buildMenuItem(itemSection);

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
     * Obtiene todos los slots para un ítem según su configuración
     * @param itemSection Sección de configuración del ítem
     * @param rows Número de filas del menú para validación
     * @return Lista de slots donde debe colocarse el ítem
     */
    private List<Integer> getItemSlots(ConfigurationSection itemSection, int rows) {
        List<Integer> slots = new ArrayList<>();
        int maxSlot = rows * 9 - 1;

        // Comprobar formato de slot único (retrocompatibilidad)
        if (itemSection.contains("slot")) {
            int slot = itemSection.getInt("slot", -1);
            if (slot >= 0 && slot <= maxSlot) {
                slots.add(slot);
            }
        }

        // Comprobar formato de rango "slots: 0-3"
        if (itemSection.contains("slots") && itemSection.isString("slots")) {
            String slotsRange = itemSection.getString("slots");
            if (slotsRange != null && slotsRange.contains("-")) {
                String[] range = slotsRange.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());

                        // Asegurar que el rango esté dentro de los límites
                        start = Math.max(0, start);
                        end = Math.min(maxSlot, end);

                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar formato inválido
                    }
                }
            }
        }

        // Comprobar formato de lista "slots: [0,1,2,3]"
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
     * @return Ítem creado
     */
    private MenuItem buildMenuItem(ConfigurationSection itemSection) {
        Material material = Material.valueOf(itemSection.getString("material", "STONE").toUpperCase());
        MenuItem menuItem = new MenuItem(material);

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
        }

        // Actualización dinámica
        if (itemSection.getBoolean("dynamic_update", false)) {
            menuItem.setDynamicUpdate(true);
            if (itemSection.contains("update_interval")) {
                menuItem.setUpdateInterval(itemSection.getLong("update_interval", 20L));
            }
        }

        return menuItem;
    }

    /**
     * Crea un menú paginado a partir de la configuración
     * @param menuId Identificador del menú en la configuración
     * @param itemSlots Posiciones donde colocar los ítems paginados
     * @return Menú paginado creado o null si no existe la configuración
     */
    public PaginationMenu buildPaginationMenu(String menuId, int... itemSlots) {
        FileConfiguration menuConfig = configManager.getConfig("menus");
        if (menuConfig == null) {
            return null;
        }

        ConfigurationSection menuSection = menuConfig.getConfigurationSection("menus." + menuId);
        if (menuSection == null) {
            return null;
        }

        // Propiedades básicas del menú
        String title = menuSection.getString("title", "Menu");
        int rows = menuSection.getInt("rows", 6);

        // Crear menú de paginación
        PaginationMenu paginationMenu = new PaginationMenu(title, rows, itemSlots);

        // Botón anterior
        ConfigurationSection prevSection = menuSection.getConfigurationSection("prev_button");
        if (prevSection != null) {
            MenuItem prevButton = buildMenuItem(prevSection);

            // Obtener slot(s) para el botón anterior
            List<Integer> prevSlots = getItemSlots(prevSection, rows);
            int prevSlot = prevSlots.isEmpty() ? (rows * 9 - 9) : prevSlots.get(0);

            paginationMenu.setPreviousPageButton(prevButton, prevSlot);
        }

        // Botón siguiente
        ConfigurationSection nextSection = menuSection.getConfigurationSection("next_button");
        if (nextSection != null) {
            MenuItem nextButton = buildMenuItem(nextSection);

            // Obtener slot(s) para el botón siguiente
            List<Integer> nextSlots = getItemSlots(nextSection, rows);
            int nextSlot = nextSlots.isEmpty() ? (rows * 9 - 1) : nextSlots.get(0);

            paginationMenu.setNextPageButton(nextButton, nextSlot);
        }

        // Ítem de relleno
        ConfigurationSection fillerSection = menuSection.getConfigurationSection("filler");
        if (fillerSection != null) {
            MenuItem fillerItem = buildMenuItem(fillerSection);
            paginationMenu.setFillerItem(fillerItem);
        }

        return paginationMenu;
    }
}