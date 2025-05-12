package net.exylia.commons.menu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Fábrica para crear menús e ítems comunes rápidamente
 */
public class MenuFactory {

    private static final Map<String, Menu> cachedMenus = new HashMap<>();

    /**
     * Crea un ítem básico de menú
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param lore Descripción del ítem
     * @return Ítem creado
     */
    public static MenuItem createItem(Material material, String name, String... lore) {
        return new MenuItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes();
    }

    /**
     * Crea un ítem decorativo (sin acción)
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @return Ítem decorativo
     */
    public static MenuItem createDecorative(Material material, String name) {
        return createItem(material, name)
                .hideAllAttributes();
    }

    /**
     * Crea un ítem brillante (con encantamiento oculto)
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param lore Descripción del ítem
     * @return Ítem brillante
     */
    public static MenuItem createGlowingItem(Material material, String name, String... lore) {
        return createItem(material, name, lore)
                .setGlowing(true);
    }

    /**
     * Crea un ítem de relleno para espacios vacíos
     * @param material Material del ítem
     * @return Ítem de relleno
     */
    public static MenuItem createFiller(Material material) {
        return new MenuItem(material)
                .setName(" ")
                .hideAllAttributes();
    }

    /**
     * Crea un ítem de relleno para espacios vacíos (vidrio tintado)
     * @return Ítem de relleno
     */
    public static MenuItem createGlassFiller() {
        return createFiller(Material.BLACK_STAINED_GLASS_PANE);
    }

    /**
     * Crea un botón de confirmación
     * @return Ítem de confirmación
     */
    public static MenuItem createConfirmButton() {
        return new MenuItem(Material.LIME_WOOL)
                .setName("&a&lConfirmar")
                .setLore("&7Haz clic para confirmar");
    }

    /**
     * Crea un botón de cancelación
     * @return Ítem de cancelación
     */
    public static MenuItem createCancelButton() {
        return new MenuItem(Material.RED_WOOL)
                .setName("&c&lCancelar")
                .setLore("&7Haz clic para cancelar");
    }

    /**
     * Crea un botón de retroceso
     * @return Ítem de retroceso
     */
    public static MenuItem createBackButton() {
        return new MenuItem(Material.ARROW)
                .setName("&8« &7Volver")
                .setLore("&7Haz clic para volver al menú anterior");
    }

    /**
     * Crea un botón de cierre
     * @return Ítem de cierre
     */
    public static MenuItem createCloseButton() {
        return new MenuItem(Material.BARRIER)
                .setName("&c&lCerrar")
                .setLore("&7Haz clic para cerrar el menú");
    }

    /**
     * Registra un menú para poder recuperarlo más tarde
     * @param name Nombre único del menú
     * @param menu Menú a registrar
     */
    public static void registerMenu(String name, Menu menu) {
        cachedMenus.put(name, menu);
    }

    /**
     * Obtiene un menú registrado
     * @param name Nombre del menú
     * @return Menú registrado o null si no existe
     */
    public static Menu getMenu(String name) {
        return cachedMenus.get(name);
    }

    /**
     * Crea un menú de confirmación
     * @param title Título del menú
     * @param onConfirm Acción al confirmar
     * @param onCancel Acción al cancelar
     * @return Menú de confirmación
     */
    public static Menu createConfirmationMenu(String title, Runnable onConfirm, Runnable onCancel) {
        Menu menu = new Menu(title, 3);

        // Botón de confirmación
        MenuItem confirmButton = createConfirmButton();
        confirmButton.setClickHandler(info -> {
            info.getPlayer().closeInventory();
            onConfirm.run();
        });
        menu.setItem(11, confirmButton);

        // Botón de cancelación
        MenuItem cancelButton = createCancelButton();
        cancelButton.setClickHandler(info -> {
            info.getPlayer().closeInventory();
            onCancel.run();
        });
        menu.setItem(15, cancelButton);

        // Rellenar espacios vacíos
        menu.fillEmptySlots(createGlassFiller());

        return menu;
    }

    /**
     * Crea un ítem con soporte de placeholders
     * @param material Material del ítem
     * @param name Nombre del ítem (admite placeholders)
     * @param lore Descripción del ítem (admite placeholders)
     * @return Ítem con soporte de placeholders
     */
    public static MenuItem createPlaceholderItem(Material material, String name, String... lore) {
        return new MenuItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .usePlaceholders(true);
    }

    /**
     * Crea un ítem dinámico que se actualiza automáticamente
     * @param material Material del ítem
     * @param name Nombre del ítem (admite placeholders)
     * @param updateInterval Intervalo de actualización en ticks
     * @param lore Descripción del ítem (admite placeholders)
     * @return Ítem dinámico
     */
    public static MenuItem createDynamicItem(Material material, String name, long updateInterval, String... lore) {
        return new MenuItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .usePlaceholders(true)
                .setDynamicUpdate(true)
                .setUpdateInterval(updateInterval);
    }

    /**
     * Crea un contador que se actualiza automáticamente
     * @param material Material del ítem
     * @param name Nombre del ítem (admite placeholders)
     * @param lore Descripción del ítem (admite placeholders)
     * @return Ítem contador
     */
    public static MenuItem createCounter(Material material, String name, String... lore) {
        // Un contador típico se actualiza cada segundo (20 ticks)
        return createDynamicItem(material, name, 20L, lore);
    }
}