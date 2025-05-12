package net.exylia.commons.menu;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Representa un menú interactivo
 */
public class Menu {
    private final Component title;
    private final int size;
    private final Map<Integer, MenuItem> items;
    private Inventory inventory;
    private Consumer<Player> closeHandler;
    private Menu returnMenu;
    private boolean dynamicUpdates = false;
    private JavaPlugin plugin;
    private long updateInterval = 20L; // 1 segundo por defecto
    private int taskId = -1;
    private Player viewer;
    private Map<Integer, Long> itemUpdateTimes = new HashMap<>();
    private Map<Integer, Integer> itemTaskIds = new HashMap<>();

    /**
     * Constructor del menú
     * @param title Título del menú (admite códigos de color)
     * @param rows Número de filas (1-6)
     */
    public Menu(String title, int rows) {
        this.title = ColorUtils.translateColors(title);
        this.size = Math.max(9, Math.min(54, rows * 9)); // Entre 1 y 6 filas
        this.items = new HashMap<>();
    }

    /**
     * Constructor del menú con componente directamente
     * @param title Componente de título ya formateado
     * @param rows Número de filas (1-6)
     */
    public Menu(Component title, int rows) {
        this.title = title;
        this.size = Math.max(9, Math.min(54, rows * 9)); // Entre 1 y 6 filas
        this.items = new HashMap<>();
    }

    /**
     * Establece un ítem en una posición del menú
     * @param slot Posición (0-53)
     * @param menuItem Ítem a colocar
     * @return El mismo menú (para encadenamiento)
     */
    public Menu setItem(int slot, MenuItem menuItem) {
        if (slot >= 0 && slot < size) {
            items.put(slot, menuItem);

            // Actualiza el inventario si ya está creado
            if (inventory != null) {
                // Si hay un viewer y el ítem usa placeholders, actualiza los placeholders
                if (viewer != null && menuItem.usesPlaceholders()) {
                    menuItem.updatePlaceholders(viewer);
                }

                inventory.setItem(slot, menuItem.getItemStack());

                // Si el ítem necesita actualizaciones dinámicas, programar su actualización
                if (viewer != null && menuItem.needsDynamicUpdate() && plugin != null) {
                    scheduleItemUpdate(slot, menuItem);
                }
            }
        }
        return this;
    }

    /**
     * Establece un manejador para cuando se cierra el menú
     * @param closeHandler Manejador de cierre
     * @return El mismo menú (para encadenamiento)
     */
    public Menu setCloseHandler(Consumer<Player> closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    /**
     * Establece un menú al que volver cuando se cierre este
     * @param returnMenu Menú al que volver
     * @return El mismo menú (para encadenamiento)
     */
    public Menu setReturnMenu(Menu returnMenu) {
        this.returnMenu = returnMenu;
        return this;
    }

    /**
     * Activa las actualizaciones dinámicas del menú
     * @param plugin Plugin para programar la tarea
     * @param tickInterval Intervalo de actualización en ticks
     * @return El mismo menú (para encadenamiento)
     */
    public Menu enableDynamicUpdates(JavaPlugin plugin, long tickInterval) {
        this.dynamicUpdates = true;
        this.plugin = plugin;
        this.updateInterval = tickInterval;
        return this;
    }

    /**
     * Desactiva las actualizaciones dinámicas del menú
     * @return El mismo menú (para encadenamiento)
     */
    public Menu disableDynamicUpdates() {
        this.dynamicUpdates = false;
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        // Cancelar todas las tareas de actualización de ítems individuales
        for (int taskId : itemTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        itemTaskIds.clear();

        return this;
    }

    /**
     * Actualiza todos los ítems en el inventario
     */
    public void updateItems() {
        if (inventory == null || viewer == null) return;

        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            MenuItem item = entry.getValue();

            // Actualizar placeholders si el ítem los usa
            if (item.usesPlaceholders()) {
                item.updatePlaceholders(viewer);
                inventory.setItem(entry.getKey(), item.getItemStack());
            }
        }
    }

    /**
     * Programa la actualización de un ítem específico
     * @param slot Slot del ítem
     * @param item Ítem a actualizar
     */
    private void scheduleItemUpdate(int slot, MenuItem item) {
        // Cancelar tarea anterior si existe
        if (itemTaskIds.containsKey(slot)) {
            Bukkit.getScheduler().cancelTask(itemTaskIds.get(slot));
        }

        // Programar nueva tarea
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (viewer != null && viewer.isOnline() && inventory != null) {
                item.updatePlaceholders(viewer);
                inventory.setItem(slot, item.getItemStack());
            } else {
                // Si el jugador ya no está viendo el inventario, cancelar la tarea
                Bukkit.getScheduler().cancelTask(itemTaskIds.getOrDefault(slot, -1));
                itemTaskIds.remove(slot);
            }
        }, item.getUpdateInterval(), item.getUpdateInterval());

        itemTaskIds.put(slot, taskId);
    }

    /**
     * Abre el menú para un jugador
     * @param player Jugador al que mostrar el menú
     */
    public void open(Player player) {
        this.viewer = player;

        // Crear inventario si no existe
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, size, title);

            // Colocar los ítems en el inventario
            for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
                MenuItem item = entry.getValue();

                // Actualizar placeholders si el ítem los usa
                if (item.usesPlaceholders()) {
                    item.updatePlaceholders(player);
                }

                inventory.setItem(entry.getKey(), item.getItemStack());
            }
        } else {
            // Si el inventario ya existe, actualizar los placeholders para el nuevo jugador
            for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
                MenuItem item = entry.getValue();
                if (item.usesPlaceholders()) {
                    item.updatePlaceholders(player);
                    inventory.setItem(entry.getKey(), item.getItemStack());
                }
            }
        }

        player.openInventory(inventory);
        MenuManager.registerOpenMenu(player, this);

        // Iniciar actualizaciones dinámicas si están activadas
        if (dynamicUpdates && plugin != null && taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateItems, updateInterval, updateInterval);
        }

        // Programar actualizaciones para ítems individuales
        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            MenuItem item = entry.getValue();
            if (item.needsDynamicUpdate() && plugin != null) {
                scheduleItemUpdate(entry.getKey(), item);
            }
        }
    }

    /**
     * Se llama cuando se cierra el menú
     * @param player Jugador que cerró el menú
     */
    void onClose(Player player) {
        // Cancelar todas las tareas de actualización
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        // Cancelar todas las tareas de actualización de ítems individuales
        for (int taskId : itemTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        itemTaskIds.clear();

        // Eliminar la referencia al viewer
        this.viewer = null;
    }

    /**
     * Obtiene un ítem por su posición
     * @param slot Posición del ítem
     * @return El ítem en esa posición o null si no hay
     */
    public MenuItem getItem(int slot) {
        return items.get(slot);
    }

    /**
     * Obtiene el manejador de cierre
     * @return Manejador de cierre
     */
    public Consumer<Player> getCloseHandler() {
        return closeHandler;
    }

    /**
     * Obtiene el menú de retorno
     * @return Menú de retorno
     */
    public Menu getReturnMenu() {
        return returnMenu;
    }

    /**
     * Llena los espacios vacíos con un ítem decorativo
     * @param filler Ítem para rellenar espacios vacíos
     * @return El mismo menú (para encadenamiento)
     */
    public Menu fillEmptySlots(MenuItem filler) {
        for (int i = 0; i < size; i++) {
            if (!items.containsKey(i)) {
                setItem(i, filler);
            }
        }
        return this;
    }

    /**
     * Crea un borde alrededor del menú
     * @param borderItem Ítem para el borde
     * @return El mismo menú (para encadenamiento)
     */
    public Menu createBorder(MenuItem borderItem) {
        int rows = size / 9;

        // Primera y última fila
        for (int i = 0; i < 9; i++) {
            setItem(i, borderItem);
            setItem(size - 9 + i, borderItem);
        }

        // Bordes laterales
        for (int i = 1; i < rows - 1; i++) {
            setItem(i * 9, borderItem);
            setItem(i * 9 + 8, borderItem);
        }

        return this;
    }

    /**
     * Comprueba si PlaceholderAPI está instalado y disponible
     * @return true si PlaceholderAPI está disponible
     */
    public static boolean isPlaceholderAPIAvailable() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Obtiene el jugador que está viendo el menú
     * @return Jugador que ve el menú
     */
    public Player getViewer() {
        return viewer;
    }
}