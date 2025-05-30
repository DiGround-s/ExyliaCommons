package net.exylia.commons.menu;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static net.exylia.commons.ExyliaPlugin.isPlaceholderAPIEnabled;

/**
 * Menú con sistema de paginación para mostrar muchos ítems
 */
public class PaginationMenu {
    private final String baseTitle;
    private final int rows;
    private final List<MenuItem> items;
    private final int[] itemSlots;
    private int currentPage = 1;
    private int maxItemsPerPage;
    private MenuItem previousPageButton;
    private MenuItem nextPageButton;
    private int previousPageButtonSlot;
    private int nextPageButtonSlot;
    private MenuItem fillerItem;
    private BiConsumer<Menu, Integer> menuCustomizer;
    private boolean usePlaceholdersInTitle = false;

    private boolean dynamicUpdates = false;
    private long updateInterval = 20L;
    private JavaPlugin plugin;
    private final Map<Player, Menu> activeMenus = new HashMap<>();
    private final Map<Player, Integer> menuTasks = new HashMap<>();
    private final Map<Player, Map<Integer, Integer>> itemTasksMap = new HashMap<>();
    private Object placeholderContext = null;

    /**
     * Constructor del menú paginado
     *
     * @param baseTitle Título base del menú (se añadirá el número de página)
     * @param rows      Número de filas (1-6)
     * @param itemSlots Posiciones donde colocar los ítems
     */
    public PaginationMenu(String baseTitle, int rows, int... itemSlots) {
        this.baseTitle = baseTitle;
        this.rows = rows;
        this.itemSlots = itemSlots;
        this.maxItemsPerPage = itemSlots.length;
        this.items = new ArrayList<>();

        // default
        this.previousPageButton = new MenuItem("ARROW")
                .setName("&8« &7Página anterior")
                .setAmount(1);

        this.nextPageButton = new MenuItem("ARROW")
                .setName("&7Página siguiente &8»")
                .setAmount(1);

        // default
        this.previousPageButtonSlot = rows * 9 - 9;
        this.nextPageButtonSlot = rows * 9 - 1;
    }

    /**
     * Añade un ítem al menú paginado
     *
     * @param item Ítem a añadir
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu addItem(MenuItem item) {
        items.add(item);
        return this;
    }

    /**
     * Añade varios ítems al menú paginado
     *
     * @param items Ítems a añadir
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu addItems(List<MenuItem> items) {
        this.items.addAll(items);
        return this;
    }

    /**
     * Establece el botón de página anterior
     *
     * @param item Ítem para el botón
     * @param slot Posición del botón
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu setPreviousPageButton(MenuItem item, int slot) {
        this.previousPageButton = item;
        this.previousPageButtonSlot = slot;
        return this;
    }

    /**
     * Establece el botón de página siguiente
     *
     * @param item Ítem para el botón
     * @param slot Posición del botón
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu setNextPageButton(MenuItem item, int slot) {
        this.nextPageButton = item;
        this.nextPageButtonSlot = slot;
        return this;
    }

    /**
     * Establece un ítem para rellenar espacios vacíos
     *
     * @param item Ítem para rellenar
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu setFillerItem(MenuItem item) {
        this.fillerItem = item;
        return this;
    }

    /**
     * Establece un personalizador para el menú
     *
     * @param customizer Función para personalizar cada página (recibe el menú y el número de página)
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu setMenuCustomizer(BiConsumer<Menu, Integer> customizer) {
        this.menuCustomizer = customizer;
        return this;
    }

    /**
     * Activa el uso de placeholders en el título del menú
     * @param use true para activar placeholders en el título
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu usePlaceholdersInTitle(boolean use) {
        this.usePlaceholdersInTitle = use;
        return this;
    }

    /**
     * Activa las actualizaciones dinámicas del menú
     * @param plugin Plugin para programar la tarea
     * @param tickInterval Intervalo de actualización en ticks
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu enableDynamicUpdates(JavaPlugin plugin, long tickInterval) {
        this.dynamicUpdates = true;
        this.plugin = plugin;
        this.updateInterval = tickInterval;
        return this;
    }

    /**
     * Desactiva las actualizaciones dinámicas del menú
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu disableDynamicUpdates() {
        this.dynamicUpdates = false;

        for (Integer taskId : menuTasks.values()) {
            if (taskId != null && taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        menuTasks.clear();

        return this;
    }

    /**
     * Abre el menú para un jugador en la página 1
     *
     * @param player Jugador al que mostrar el menú
     */
    public void open(Player player) {
        open(player, 1);
    }

    /**
     * Abre el menú para un jugador en una página específica
     *
     * @param player Jugador al que mostrar el menú
     * @param page   Número de página
     */
    public void open(Player player, int page) {
        if (menuTasks.containsKey(player)) {
            Integer taskId = menuTasks.get(player);
            if (taskId != null && taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            menuTasks.remove(player);
        }

        if (itemTasksMap.containsKey(player)) {
            Map<Integer, Integer> itemTasks = itemTasksMap.get(player);
            for (Integer taskId : itemTasks.values()) {
                if (taskId != null && taskId != -1) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }
            }
            itemTasksMap.remove(player);
        }

        Menu previousMenu = activeMenus.get(player);
        if (previousMenu != null) {
            previousMenu.onClose(player);
        }

        int maxPages = (int) Math.ceil((double) items.size() / maxItemsPerPage);
        maxPages = Math.max(1, maxPages);

        if (page < 1) {
            page = 1;
        } else if (page > maxPages) {
            page = maxPages;
        }

        currentPage = page;

        Menu menu = createMenuForPage(player, page, maxPages);

        activeMenus.put(player, menu);

        if (!itemTasksMap.containsKey(player)) {
            itemTasksMap.put(player, new HashMap<>());
        }

        if (dynamicUpdates && plugin != null) {
            scheduleItemUpdates(player, menu);
        }

        menu.open(player);
    }

    /**
     * Programa las actualizaciones individuales para cada ítem según su configuración
     * @param player Jugador dueño del menú
     * @param menu Menú a actualizar
     */
    private void scheduleItemUpdates(Player player, Menu menu) {
        Map<Integer, Integer> itemTasks = itemTasksMap.get(player);

        for (int slot = 0; slot < rows * 9; slot++) {
            MenuItem item = menu.getItem(slot);

            if (item != null && item.needsDynamicUpdate() && item.usesPlaceholders()) {
                final int finalSlot = slot;

                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    if (player.isOnline() && activeMenus.containsKey(player)) {
                        updateSingleItem(player, menu, finalSlot, item);
                    } else {
                        Integer existingTaskId = itemTasks.get(finalSlot);
                        if (existingTaskId != null && existingTaskId != -1) {
                            Bukkit.getScheduler().cancelTask(existingTaskId);
                        }
                        itemTasks.remove(finalSlot);
                    }
                }, item.getUpdateInterval(), item.getUpdateInterval());

                itemTasks.put(slot, taskId);
            }
        }
    }

    /**
     * Actualiza un único ítem en el menú
     * @param player Jugador dueño del menú
     * @param menu Menú donde está el ítem
     * @param slot Posición del ítem
     * @param item El ítem a actualizar
     */
    private void updateSingleItem(Player player, Menu menu, int slot, MenuItem item) {
        if (menu == null || !player.isOnline()) return;

        item.updatePlaceholders(item.getPlaceholderPlayer() != null ? item.getPlaceholderPlayer() : player);

        menu.setItem(slot, item);
    }

    /**
     * Limpia todas las tareas y referencias cuando un jugador cierra definitivamente el menú
     * @param player Jugador que cerró el menú
     */
    public void cleanup(Player player) {
        if (menuTasks.containsKey(player)) {
            Integer taskId = menuTasks.get(player);
            if (taskId != null && taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            menuTasks.remove(player);
        }

        if (itemTasksMap.containsKey(player)) {
            Map<Integer, Integer> itemTasks = itemTasksMap.get(player);
            for (Integer taskId : itemTasks.values()) {
                if (taskId != null && taskId != -1) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }
            }
            itemTasksMap.remove(player);
        }

        activeMenus.remove(player);
    }

    /**
     * Establece un objeto de contexto para los placeholders personalizados
     * @param context Objeto de contexto para placeholders personalizados
     * @return El mismo menú paginado (para encadenamiento)
     */
    public PaginationMenu setPlaceholderContext(Object context) {
        this.placeholderContext = context;
        return this;
    }

    /**
     * Crea un menú para una página específica
     *
     * @param player   Jugador para quien se crea el menú
     * @param page     Número de página
     * @param maxPages Número total de páginas
     * @return Menú configurado para la página
     */
    private Menu createMenuForPage(Player player, int page, int maxPages) {
        String title = baseTitle.replace("%page%", String.valueOf(page)).replace("%pages%", String.valueOf(maxPages));

        if (placeholderContext != null) {
            title = CustomPlaceholderManager.process(title, placeholderContext);
        }

        if (usePlaceholdersInTitle && isPlaceholderAPIEnabled()) {
            title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, title);
        }

        Menu menu = new Menu(title, rows);

        menu.setCloseHandler(p -> {
            if (!activeMenus.containsKey(p) || activeMenus.get(p) != menu) {
                cleanup(p);
            }
        });

        if (page > 1) {
            MenuItem prevButton = previousPageButton.clone();
            if (prevButton.usesPlaceholders()) {
                prevButton.updatePlaceholders(player);
            }
            prevButton.setClickHandler(info -> open(info.getPlayer(), page - 1));
            menu.setItem(previousPageButtonSlot, prevButton);
        }

        if (page < maxPages) {
            MenuItem nextButton = nextPageButton.clone();
            if (nextButton.usesPlaceholders()) {
                nextButton.updatePlaceholders(player);
            }
            nextButton.setClickHandler(info -> open(info.getPlayer(), page + 1));
            menu.setItem(nextPageButtonSlot, nextButton);
        }

        int start = (page - 1) * maxItemsPerPage;
        int end = Math.min(start + maxItemsPerPage, items.size());

        for (int i = start, slot = 0; i < end; i++, slot++) {
            if (slot < itemSlots.length) {
                MenuItem item = items.get(i).clone();
                if (item.usesPlaceholders()) {
                    item.updatePlaceholders(player);
                }
                menu.setItem(itemSlots[slot], item);
            }
        }

        if (fillerItem != null) {
            MenuItem filler = fillerItem.clone();
            if (filler.usesPlaceholders()) {
                filler.updatePlaceholders(player);
            }

            for (int i = 0; i < rows * 9; i++) {
                if (menu.getItem(i) == null) {
                    menu.setItem(i, filler);
                }
            }
        }

        if (menuCustomizer != null) {
            menuCustomizer.accept(menu, page);
        }

        return menu;
    }

    /**
     * Clona un menú de paginación
     * @return Copia del menú de paginación
     */
    public PaginationMenu clone() {
        PaginationMenu clone = new PaginationMenu(baseTitle, rows, itemSlots);

        for (MenuItem item : items) {
            clone.addItem(item.clone());
        }

        if (previousPageButton != null) {
            clone.setPreviousPageButton(previousPageButton.clone(), previousPageButtonSlot);
        }

        if (nextPageButton != null) {
            clone.setNextPageButton(nextPageButton.clone(), nextPageButtonSlot);
        }

        if (fillerItem != null) {
            clone.setFillerItem(fillerItem.clone());
        }

        clone.usePlaceholdersInTitle(usePlaceholdersInTitle);

        if (dynamicUpdates) {
            clone.enableDynamicUpdates(plugin, updateInterval);
        }

        if (menuCustomizer != null) {
            clone.setMenuCustomizer(menuCustomizer);
        }

        return clone;
    }
}