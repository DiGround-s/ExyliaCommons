package net.exylia.commons.menu;

import net.exylia.commons.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

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
                    int slot = itemSection.getInt("slot", -1);
                    if (slot >= 0 && slot < rows * 9) {
                        MenuItem menuItem = buildMenuItem(itemSection);
                        menu.setItem(slot, menuItem);
                    }
                }
            }
        }

        return menu;
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
            int prevSlot = prevSection.getInt("slot", rows * 9 - 9);
            paginationMenu.setPreviousPageButton(prevButton, prevSlot);
        }

        // Botón siguiente
        ConfigurationSection nextSection = menuSection.getConfigurationSection("next_button");
        if (nextSection != null) {
            MenuItem nextButton = buildMenuItem(nextSection);
            int nextSlot = nextSection.getInt("slot", rows * 9 - 1);
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