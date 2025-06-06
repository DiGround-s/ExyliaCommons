package net.exylia.commons.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.exylia.commons.ExyliaPlugin.isPlaceholderAPIEnabled;

public class PaginationMenu extends Menu {
    private final List<MenuItem> paginationItems;
    private final int[] itemSlots;
    private int currentPage = 1;
    private int maxItemsPerPage;

    // Botones de navegación
    private MenuItem previousPageButton;
    private MenuItem nextPageButton;
    private int previousPageButtonSlot;
    private int nextPageButtonSlot;

    // Customización - Fillers
    private MenuItem globalFillerItem;      // Filler para toda la GUI
    private MenuItem itemSlotsFillerItem;   // Filler específico para slots de items vacíos
    private BiConsumer<Menu, Integer> menuCustomizer;

    // Control de paginación
    private final Map<Player, Integer> playerCurrentPage = new HashMap<>();
    private final Map<Player, Integer> menuTasks = new HashMap<>();
    private final Map<Player, Map<Integer, Integer>> itemTasksMap = new HashMap<>();

    public PaginationMenu(String baseTitle, int rows, int... itemSlots) {
        super(baseTitle, rows);

        this.paginationItems = new ArrayList<>();
        this.itemSlots = itemSlots;
        this.maxItemsPerPage = itemSlots.length;

        // Botones por defecto
        this.previousPageButton = new MenuItem("headbase-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGExZDU1YjNmOTg5NDEwYTM0NzUyNjUwZTI0OGM5YjZjMTc4M2E3ZWMyYWEzZmQ3Nzg3YmRjNGQwZTYzN2QzOSJ9fX0=")
                .setName("<#8fffc1>⏪ <#8fffc1>Anterior")
                .setLore("&8⏵ <#e7cfff>Para ir a la siguiente página.")
                .hideAllAttributes()
                .setAmount(1);

        this.nextPageButton = new MenuItem("headurl-http://textures.minecraft.net/texture/fa87e3d96e1cfeb9ccfb3ba53a217faf5249e285533b271a2fb284c30dbd9829")
                .setName("<#8fffc1>Siguiente <#a1ffc3>⏩")
                .setLore("&8⏵ <#e7cfff>Para ir a la página anterior.")
                .hideAllAttributes()
                .setAmount(1);

        // Posiciones por defecto
        this.previousPageButtonSlot = rows * 9 - 9;
        this.nextPageButtonSlot = rows * 9 - 1;

        super.setCloseHandler(this::onPlayerCloseMenu);
    }

    // ==================== PAGINATION SPECIFIC METHODS ====================

    public PaginationMenu addItem(MenuItem item) {
        paginationItems.add(item);
        return this;
    }

    public PaginationMenu addItems(List<MenuItem> items) {
        this.paginationItems.addAll(items);
        return this;
    }

    public PaginationMenu setPreviousPageButton(MenuItem item, int slot) {
        this.previousPageButton = item;
        this.previousPageButtonSlot = slot;
        return this;
    }

    public PaginationMenu setNextPageButton(MenuItem item, int slot) {
        this.nextPageButton = item;
        this.nextPageButtonSlot = slot;
        return this;
    }

    /**
     * Establece un filler global que se aplicará a todos los slots vacíos
     * @param item Item filler global
     * @return El mismo menú para encadenamiento
     */
    public PaginationMenu setGlobalFillerItem(MenuItem item) {
        this.globalFillerItem = item;
        return this;
    }

    /**
     * Establece un filler específico para los slots de items que estén vacíos
     * @param item Item filler para slots de items
     * @return El mismo menú para encadenamiento
     */
    public PaginationMenu setItemSlotsFillerItem(MenuItem item) {
        this.itemSlotsFillerItem = item;
        return this;
    }

    /**
     * @deprecated Usar setGlobalFillerItem() o setItemSlotsFillerItem()
     */
    @Deprecated
    public PaginationMenu setFillerItem(MenuItem item) {
        this.globalFillerItem = item;
        return this;
    }

    public PaginationMenu setMenuCustomizer(BiConsumer<Menu, Integer> customizer) {
        this.menuCustomizer = customizer;
        return this;
    }

    // ==================== OVERRIDE MENU METHODS ====================

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    public void open(Player player, int page) {
        cleanupPlayerResources(player);
        int maxPages = Math.max(1, (int) Math.ceil((double) paginationItems.size() / maxItemsPerPage));
        page = Math.max(1, Math.min(page, maxPages));
        playerCurrentPage.put(player, page);
        currentPage = page;
        updateMenuTitle(player, page, maxPages);

        // Aplicar fillers usando el sistema de capas
        applyFillers(player, page);

        // Agregar navegación y items
        setupNavigationButtons(player, page, maxPages);
        addItemsForPage(player, page);

        if (menuCustomizer != null) {
            menuCustomizer.accept(this, page);
        }
        super.open(player);
        if (super.dynamicUpdates && super.plugin != null) {
            scheduleItemUpdates(player);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void updateMenuTitle(Player player, int page, int maxPages) {
        String newTitle = super.rawTitle
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(maxPages));

        if (super.usePlaceholdersInTitle) {
            if (super.titlePlaceholderContext != null) {
                newTitle = CustomPlaceholderManager.process(newTitle, super.titlePlaceholderContext);
            }

            if (isPlaceholderAPIEnabled()) {
                newTitle = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, newTitle);
            }
        }
        super.title = net.exylia.commons.utils.ColorUtils.parse(newTitle);
    }

    /**
     * Aplica los fillers usando un sistema de capas similar a MultiPaginationMenu
     */
    private void applyFillers(Player player, int page) {
        // PASO 1: Guardar items ya establecidos manualmente (antes del open) - excluyendo slots de paginación
        Map<Integer, MenuItem> manualItems = new HashMap<>();
        for (Map.Entry<Integer, MenuItem> entry : super.items.entrySet()) {
            int slot = entry.getKey();
            // Solo preservar items que NO están en slots de paginación
            boolean isItemSlot = false;
            for (int itemSlot : itemSlots) {
                if (slot == itemSlot) {
                    isItemSlot = true;
                    break;
                }
            }
            if (!isItemSlot) {
                manualItems.put(slot, entry.getValue());
            }
        }

        // PASO 2: Limpiar TODOS los items (incluyendo slots de paginación de páginas anteriores)
        super.items.clear();

        // PASO 3: Aplicar filler global primero (capa base), pero no en slots con items manuales
        if (globalFillerItem != null) {
            for (int i = 0; i < super.size; i++) {
                if (!manualItems.containsKey(i)) {
                    MenuItem fillerClone = globalFillerItem.clone();
                    if (fillerClone.usesPlaceholders()) {
                        fillerClone.updatePlaceholders(player);
                    }
                    super.setItem(i, fillerClone);
                }
            }
        }

        // PASO 4: Aplicar filler específico para item slots vacíos (sobrescribe el global en estos slots)
        if (itemSlotsFillerItem != null) {
            // Calcular qué slots de items estarán vacíos en esta página
            int start = (page - 1) * maxItemsPerPage;
            int end = Math.min(start + maxItemsPerPage, paginationItems.size());
            int itemsInPage = end - start;

            // Aplicar filler a los slots de items que estarán vacíos, pero no en slots con items manuales
            for (int i = itemsInPage; i < itemSlots.length; i++) {
                int slot = itemSlots[i];
                if (!manualItems.containsKey(slot)) {
                    MenuItem fillerClone = itemSlotsFillerItem.clone();
                    if (fillerClone.usesPlaceholders()) {
                        fillerClone.updatePlaceholders(player);
                    }
                    super.setItem(slot, fillerClone);
                }
            }
        }

        // PASO 5: Restaurar items establecidos manualmente (que NO son de paginación)
        for (Map.Entry<Integer, MenuItem> entry : manualItems.entrySet()) {
            super.setItem(entry.getKey(), entry.getValue());
        }
    }

    private void setupNavigationButtons(Player player, int page, int maxPages) {
        if (page > 1) {
            MenuItem prevButton = previousPageButton.clone();
            if (prevButton.usesPlaceholders()) {
                prevButton.updatePlaceholders(player);
            }
            prevButton.setClickHandler(info -> open(info.player(), page - 1));
            super.setItem(previousPageButtonSlot, prevButton);
        }

        if (page < maxPages) {
            MenuItem nextButton = nextPageButton.clone();
            if (nextButton.usesPlaceholders()) {
                nextButton.updatePlaceholders(player);
            }
            nextButton.setClickHandler(info -> open(info.player(), page + 1));
            super.setItem(nextPageButtonSlot, nextButton);
        }
    }

    private void addItemsForPage(Player player, int page) {
        int start = (page - 1) * maxItemsPerPage;
        int end = Math.min(start + maxItemsPerPage, paginationItems.size());

        for (int i = start, slot = 0; i < end; i++, slot++) {
            if (slot < itemSlots.length) {
                MenuItem item = paginationItems.get(i).clone();
                if (item.usesPlaceholders()) {
                    item.updatePlaceholders(player);
                }
                super.setItem(itemSlots[slot], item);
            }
        }
    }

    /**
     * @deprecated No se usa más, la lógica está en applyFillers()
     */
    @Deprecated
    private void clearAllItems() {
        for (int slot : itemSlots) {
            super.items.remove(slot);
        }
    }

    private void scheduleItemUpdates(Player player) {
        if (!itemTasksMap.containsKey(player)) {
            itemTasksMap.put(player, new HashMap<>());
        }

        Map<Integer, Integer> itemTasks = itemTasksMap.get(player);
        for (int i = 0; i < super.size; i++) {
            MenuItem item = super.getItem(i);
            if (item != null && item.needsDynamicUpdate() && item.usesPlaceholders()) {
                final int finalSlot = i;

                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(super.plugin, () -> {
                    if (player.isOnline() && super.getViewer() == player) {
                        item.updatePlaceholders(player);
                        super.setItem(finalSlot, item);
                    } else {
                        // Limpiar tarea si el jugador ya no está viendo el menú
                        Integer existingTaskId = itemTasks.get(finalSlot);
                        if (existingTaskId != null) {
                            Bukkit.getScheduler().cancelTask(existingTaskId);
                        }
                        itemTasks.remove(finalSlot);
                    }
                }, item.getUpdateInterval(), item.getUpdateInterval());

                itemTasks.put(i, taskId);
            }
        }
    }

    private void onPlayerCloseMenu(Player player) {
        cleanupPlayerResources(player);
        if (externalCloseHandler != null) {
            externalCloseHandler.accept(player);
        }
    }

    private void cleanupPlayerResources(Player player) {
        playerCurrentPage.remove(player);

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
    }

    // ==================== PUBLIC UTILITY METHODS ====================

    public int getCurrentPage(Player player) {
        return playerCurrentPage.getOrDefault(player, 1);
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) paginationItems.size() / maxItemsPerPage));
    }

    public List<MenuItem> getPaginationItems() {
        return new ArrayList<>(paginationItems);
    }

    public PaginationMenu clearPaginationItems() {
        paginationItems.clear();
        return this;
    }

    public MenuItem getGlobalFillerItem() {
        return globalFillerItem;
    }

    public MenuItem getItemSlotsFillerItem() {
        return itemSlotsFillerItem;
    }

    public void cleanup(Player player) {
        cleanupPlayerResources(player);
    }

    // ==================== OVERRIDE CHAIN METHODS ====================

    private Consumer<Player> externalCloseHandler = null;

    @Override
    public PaginationMenu setCloseHandler(Consumer<Player> closeHandler) {
        this.externalCloseHandler = closeHandler;
        super.setCloseHandler(this::onPlayerCloseMenu);
        return this;
    }

    @Override
    public PaginationMenu setReturnMenu(Menu returnMenu) {
        super.setReturnMenu(returnMenu);
        return this;
    }

    @Override
    public PaginationMenu enableDynamicUpdates(JavaPlugin plugin, long tickInterval) {
        super.enableDynamicUpdates(plugin, tickInterval);
        return this;
    }

    @Override
    public PaginationMenu disableDynamicUpdates() {
        super.disableDynamicUpdates();

        for (Integer taskId : menuTasks.values()) {
            if (taskId != null && taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        menuTasks.clear();

        for (Map<Integer, Integer> itemTasks : itemTasksMap.values()) {
            for (Integer taskId : itemTasks.values()) {
                if (taskId != null && taskId != -1) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }
            }
        }
        itemTasksMap.clear();

        return this;
    }

    // ==================== CLONE METHOD ====================

    public PaginationMenu clone() {
        PaginationMenu cloned = new PaginationMenu(super.rawTitle != null ? super.rawTitle : "Cloned Menu", super.size / 9, itemSlots);

        for (MenuItem item : paginationItems) {
            cloned.addItem(item.clone());
        }
        if (previousPageButton != null) {
            cloned.setPreviousPageButton(previousPageButton.clone(), previousPageButtonSlot);
        }
        if (nextPageButton != null) {
            cloned.setNextPageButton(nextPageButton.clone(), nextPageButtonSlot);
        }

        if (globalFillerItem != null) {
            cloned.setGlobalFillerItem(globalFillerItem.clone());
        }
        if (itemSlotsFillerItem != null) {
            cloned.setItemSlotsFillerItem(itemSlotsFillerItem.clone());
        }
        if (menuCustomizer != null) {
            cloned.setMenuCustomizer(menuCustomizer);
        }

        cloned.usePlaceholdersInTitle(super.usePlaceholdersInTitle);
        if (super.dynamicUpdates) {
            cloned.enableDynamicUpdates(super.plugin, super.updateInterval);
        }

        return cloned;
    }
}