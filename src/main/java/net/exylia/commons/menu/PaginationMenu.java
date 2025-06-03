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

    // Customización
    private MenuItem fillerItem;
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
        this.previousPageButton = new MenuItem("ARROW")
                .setName("&8« &7Página anterior")
                .setAmount(1);

        this.nextPageButton = new MenuItem("ARROW")
                .setName("&7Página siguiente &8»")
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

    public PaginationMenu setFillerItem(MenuItem item) {
        this.fillerItem = item;
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
        clearAllItems();
        setupNavigationButtons(player, page, maxPages);
        addItemsForPage(player, page);
        if (fillerItem != null) {
            fillEmptySlots(fillerItem);
        }
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
            cloned.setPreviousPageButton(nextPageButton.clone(), nextPageButtonSlot);
        }

        if (fillerItem != null) {
            cloned.setFillerItem(fillerItem.clone());
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