package net.exylia.commons.menu;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

/**
 * Sistema de gestión de menús interactivos para plugins de Exylia
 */
public class MenuManager implements Listener {
    private static JavaPlugin plugin;
    private static final Map<UUID, Menu> openMenus = new HashMap<>();
    private static final Map<UUID, PaginationMenu> openPaginationMenus = new HashMap<>();
    private static boolean initialized = false;
    private static boolean placeholderAPIEnabled = false;

    /**
     * Inicializa el sistema de menús
     * @param javaPlugin El plugin principal
     */
    public static void initialize(JavaPlugin javaPlugin) {
        if (initialized) return;
        plugin = javaPlugin;
        Bukkit.getPluginManager().registerEvents(new MenuManager(), plugin);
        initialized = true;

        // Comprobar si PlaceholderAPI está instalado
        placeholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        if (placeholderAPIEnabled) {
            plugin.getLogger().info("PlaceholderAPI detectado. Soporte de placeholders activado en menús.");
        }
    }

    /**
     * Maneja los clics en inventarios
     * @param event Evento de clic en inventario
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Menu menu = openMenus.get(player.getUniqueId());
        if (menu == null) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        MenuItem item = menu.getItem(event.getSlot());
        if (item != null && item.getClickHandler() != null) {
            item.getClickHandler().accept(new MenuClickInfo(player, event.getClick(), event.getSlot(), menu));
        }
    }

    /**
     * Evita arrastrar items en menús
     * @param event Evento de arrastrar en inventario
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (openMenus.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /**
     * Maneja el cierre de inventarios
     * @param event Evento de cierre de inventario
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Menu menu = openMenus.get(player.getUniqueId());
        if (menu == null) return;

        // Notificar al menú que fue cerrado para limpiar recursos
        menu.onClose(player);

        // Si este menú era parte de un PaginationMenu, manejar la limpieza a través de él
        PaginationMenu paginationMenu = openPaginationMenus.get(player.getUniqueId());
        if (paginationMenu != null) {
            // Limpiar recursos del menú paginado
            paginationMenu.cleanup(player);
            openPaginationMenus.remove(player.getUniqueId());
        }

        openMenus.remove(player.getUniqueId());

        if (menu.getCloseHandler() != null) {
            menu.getCloseHandler().accept(player);
        }

        // Si hay un menú para regresar, ábrelo automáticamente
        if (menu.getReturnMenu() != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> menu.getReturnMenu().open(player), 1L);
        }
    }

    /**
     * Registra un menú abierto
     * @param player Jugador que abrió el menú
     * @param menu Menú abierto
     */
    static void registerOpenMenu(Player player, Menu menu) {
        openMenus.put(player.getUniqueId(), menu);
    }

    /**
     * Registra un menú paginado abierto
     * @param player Jugador que abrió el menú
     * @param paginationMenu Menú paginado abierto
     */
    static void registerOpenPaginationMenu(Player player, PaginationMenu paginationMenu) {
        openPaginationMenus.put(player.getUniqueId(), paginationMenu);
    }

    /**
     * Verifica si PlaceholderAPI está habilitado
     * @return true si PlaceholderAPI está disponible
     */
    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    /**
     * Obtiene el plugin asociado
     * @return Plugin principal
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}