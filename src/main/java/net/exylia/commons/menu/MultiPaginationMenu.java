package net.exylia.commons.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Menú con múltiples secciones paginables independientes
 */
public class MultiPaginationMenu extends Menu {

    /**
     * Representa una sección paginable dentro del menú
     */
    public static class PaginationSection {
        private final String name;
        private final List<MenuItem> items;
        private final int[] slots;
        private int currentPage = 1;

        // Botones de navegación
        private MenuItem previousButton;
        private MenuItem nextButton;
        private int previousButtonSlot;
        private int nextButtonSlot;

        // Filler para esta sección
        private MenuItem fillerItem;

        // Callback cuando se selecciona un item
        private BiConsumer<MenuClickInfo, Integer> onItemSelect;

        // Item seleccionado actualmente
        private Integer selectedIndex = null;
        private MenuItem selectedItemTemplate;

        public PaginationSection(String name, int[] slots) {
            this.name = name;
            this.items = new ArrayList<>();
            this.slots = slots;
        }

        public PaginationSection addItem(MenuItem item) {
            items.add(item);
            return this;
        }

        public PaginationSection addItems(List<MenuItem> items) {
            this.items.addAll(items);
            return this;
        }

        public PaginationSection setPreviousButton(MenuItem button, int slot) {
            this.previousButton = button;
            this.previousButtonSlot = slot;
            return this;
        }

        public PaginationSection setNextButton(MenuItem button, int slot) {
            this.nextButton = button;
            this.nextButtonSlot = slot;
            return this;
        }

        public PaginationSection setFillerItem(MenuItem filler) {
            this.fillerItem = filler;
            return this;
        }

        public PaginationSection setOnItemSelect(BiConsumer<MenuClickInfo, Integer> handler) {
            this.onItemSelect = handler;
            return this;
        }

        public PaginationSection setSelectedItemTemplate(MenuItem template) {
            this.selectedItemTemplate = template;
            return this;
        }

        public void setSelectedIndex(Integer index) {
            this.selectedIndex = index;
        }

        public Integer getSelectedIndex() {
            return selectedIndex;
        }

        public int getTotalPages() {
            return Math.max(1, (int) Math.ceil((double) items.size() / slots.length));
        }

        public List<MenuItem> getItemsForPage(int page) {
            int start = (page - 1) * slots.length;
            int end = Math.min(start + slots.length, items.size());

            if (start >= items.size()) {
                return new ArrayList<>();
            }

            return items.subList(start, end);
        }

        public boolean isItemSelected(int globalIndex) {
            return selectedIndex != null && selectedIndex == globalIndex;
        }

        public MenuItem getItem(int index) {
            if (index >= 0 && index < items.size()) {
                return items.get(index);
            }
            return null;
        }

        public int getItemCount() {
            return items.size();
        }
    }

    private final Map<String, PaginationSection> sections = new LinkedHashMap<>();
    private final Map<Player, Map<String, Integer>> playerPages = new HashMap<>();
    private final Map<Player, Map<String, Map<Integer, Integer>>> playerItemTasks = new HashMap<>();

    // Callback global cuando se actualiza cualquier sección
    private BiConsumer<String, Integer> onSectionUpdate;

    public MultiPaginationMenu(String title, int rows) {
        super(title, rows);
        super.setCloseHandler(this::onPlayerCloseMenu);
    }

    /**
     * Añade una nueva sección paginable
     * @param name Nombre único de la sección
     * @param slots Slots donde se mostrarán los items
     * @return La sección creada
     */
    public PaginationSection addSection(String name, int... slots) {
        PaginationSection section = new PaginationSection(name, slots);
        sections.put(name, section);
        return section;
    }

    /**
     * Obtiene una sección por nombre
     * @param name Nombre de la sección
     * @return La sección o null si no existe
     */
    public PaginationSection getSection(String name) {
        return sections.get(name);
    }

    /**
     * Establece un callback global cuando se actualiza alguna sección
     * @param callback Callback que recibe el nombre de la sección y la página actual
     * @return El mismo menú
     */
    public MultiPaginationMenu setOnSectionUpdate(BiConsumer<String, Integer> callback) {
        this.onSectionUpdate = callback;
        return this;
    }

    @Override
    public void open(Player player) {
        cleanupPlayerResources(player);

        // Inicializar páginas del jugador si no existen
        if (!playerPages.containsKey(player)) {
            Map<String, Integer> pages = new HashMap<>();
            for (String sectionName : sections.keySet()) {
                pages.put(sectionName, 1);
            }
            playerPages.put(player, pages);
        }

        updateMenu(player);
        super.open(player);

        if (super.dynamicUpdates && super.plugin != null) {
            scheduleItemUpdates(player);
        }
    }

    /**
     * Actualiza una sección específica para un jugador
     * @param player Jugador
     * @param sectionName Nombre de la sección
     * @param page Nueva página
     */
    public void updateSection(Player player, String sectionName, int page) {
        PaginationSection section = sections.get(sectionName);
        if (section == null) return;

        Map<String, Integer> pages = playerPages.get(player);
        if (pages == null) return;

        int maxPages = section.getTotalPages();
        page = Math.max(1, Math.min(page, maxPages));
        pages.put(sectionName, page);

        updateMenu(player);

        if (onSectionUpdate != null) {
            onSectionUpdate.accept(sectionName, page);
        }
    }

    private void updateMenu(Player player) {
        Map<String, Integer> pages = playerPages.get(player);
        if (pages == null) return;

        // Limpiar todos los items
        super.items.clear();

        // Actualizar cada sección
        for (Map.Entry<String, PaginationSection> entry : sections.entrySet()) {
            String sectionName = entry.getKey();
            PaginationSection section = entry.getValue();
            int currentPage = pages.getOrDefault(sectionName, 1);

            // Colocar items de la página actual
            List<MenuItem> pageItems = section.getItemsForPage(currentPage);
            for (int i = 0; i < pageItems.size() && i < section.slots.length; i++) {
                int slot = section.slots[i];
                MenuItem item = pageItems.get(i).clone();

                // Calcular el índice global del item
                int globalIndex = (currentPage - 1) * section.slots.length + i;

                // Si este item está seleccionado y hay un template especial, usarlo
                if (section.isItemSelected(globalIndex) && section.selectedItemTemplate != null) {
                    // Clonar el template y aplicar los placeholders del item original
                    MenuItem selectedItem = section.selectedItemTemplate.clone();
                    if (selectedItem.usesPlaceholders()) {
                        selectedItem.setPlaceholderContext(item.getPlaceholderContext());
                        selectedItem.updatePlaceholders(player);
                    }
                    item = selectedItem;
                }

                if (item.usesPlaceholders()) {
                    item.updatePlaceholders(player);
                }

                // Configurar el click handler
                final int finalGlobalIndex = globalIndex;
                final String finalSectionName = sectionName;
                Consumer<MenuClickInfo> originalHandler = item.getClickHandler();

                item.setClickHandler(clickInfo -> {
                    // Primero ejecutar el handler de selección si existe
                    if (section.onItemSelect != null) {
                        section.onItemSelect.accept(clickInfo, finalGlobalIndex);
                    }

                    // Marcar como seleccionado
                    section.setSelectedIndex(finalGlobalIndex);

                    // Actualizar el menú para reflejar la selección
                    updateMenu(player);

                    // Ejecutar el handler original si existe
                    if (originalHandler != null) {
                        originalHandler.accept(clickInfo);
                    }
                });

                super.setItem(slot, item);
            }

            // Colocar filler en slots vacíos de esta sección
            if (section.fillerItem != null) {
                for (int slot : section.slots) {
                    if (super.getItem(slot) == null) {
                        super.setItem(slot, section.fillerItem.clone());
                    }
                }
            }

            // Colocar botones de navegación
            if (currentPage > 1 && section.previousButton != null) {
                MenuItem prevButton = section.previousButton.clone();
                if (prevButton.usesPlaceholders()) {
                    prevButton.updatePlaceholders(player);
                }
                prevButton.setClickHandler(info -> updateSection(player, sectionName, currentPage - 1));
                super.setItem(section.previousButtonSlot, prevButton);
            }

            if (currentPage < section.getTotalPages() && section.nextButton != null) {
                MenuItem nextButton = section.nextButton.clone();
                if (nextButton.usesPlaceholders()) {
                    nextButton.updatePlaceholders(player);
                }
                nextButton.setClickHandler(info -> updateSection(player, sectionName, currentPage + 1));
                super.setItem(section.nextButtonSlot, nextButton);
            }
        }

        // Si el inventario ya está abierto, actualizar los items
        if (super.getViewer() == player) {
            for (Map.Entry<Integer, MenuItem> entry : super.items.entrySet()) {
                super.inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
    }

    /**
     * Programa las actualizaciones de items dinámicos
     */
    private void scheduleItemUpdates(Player player) {
        if (!playerItemTasks.containsKey(player)) {
            playerItemTasks.put(player, new HashMap<>());
        }

        Map<String, Map<Integer, Integer>> sectionTasks = playerItemTasks.get(player);

        for (Map.Entry<String, PaginationSection> sectionEntry : sections.entrySet()) {
            String sectionName = sectionEntry.getKey();
            PaginationSection section = sectionEntry.getValue();

            if (!sectionTasks.containsKey(sectionName)) {
                sectionTasks.put(sectionName, new HashMap<>());
            }

            Map<Integer, Integer> itemTasks = sectionTasks.get(sectionName);

            for (int slot : section.slots) {
                MenuItem item = super.getItem(slot);
                if (item != null && item.needsDynamicUpdate() && item.usesPlaceholders()) {
                    final int finalSlot = slot;

                    int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(super.plugin, () -> {
                        if (player.isOnline() && super.getViewer() == player) {
                            MenuItem currentItem = super.getItem(finalSlot);
                            if (currentItem != null) {
                                currentItem.updatePlaceholders(player);
                                super.inventory.setItem(finalSlot, currentItem.getItemStack());
                            }
                        } else {
                            Integer existingTaskId = itemTasks.get(finalSlot);
                            if (existingTaskId != null) {
                                Bukkit.getScheduler().cancelTask(existingTaskId);
                            }
                            itemTasks.remove(finalSlot);
                        }
                    }, item.getUpdateInterval(), item.getUpdateInterval());

                    itemTasks.put(slot, taskId);
                }
            }
        }
    }

    /**
     * Limpia recursos cuando se cierra el menú
     */
    private void onPlayerCloseMenu(Player player) {
        cleanupPlayerResources(player);
        if (externalCloseHandler != null) {
            externalCloseHandler.accept(player);
        }
    }

    /**
     * Limpia todos los recursos asociados a un jugador
     */
    private void cleanupPlayerResources(Player player) {
        playerPages.remove(player);

        if (playerItemTasks.containsKey(player)) {
            Map<String, Map<Integer, Integer>> sectionTasks = playerItemTasks.get(player);
            for (Map<Integer, Integer> itemTasks : sectionTasks.values()) {
                for (Integer taskId : itemTasks.values()) {
                    if (taskId != null && taskId != -1) {
                        Bukkit.getScheduler().cancelTask(taskId);
                    }
                }
            }
            playerItemTasks.remove(player);
        }
    }

    /**
     * Obtiene la página actual de una sección para un jugador
     * @param player Jugador
     * @param sectionName Nombre de la sección
     * @return Página actual (1 si no está definida)
     */
    public int getCurrentPage(Player player, String sectionName) {
        Map<String, Integer> pages = playerPages.get(player);
        if (pages != null) {
            return pages.getOrDefault(sectionName, 1);
        }
        return 1;
    }

    /**
     * Obtiene el índice del item seleccionado en una sección
     * @param sectionName Nombre de la sección
     * @return Índice del item seleccionado o null
     */
    public Integer getSelectedIndex(String sectionName) {
        PaginationSection section = sections.get(sectionName);
        return section != null ? section.getSelectedIndex() : null;
    }

    /**
     * Obtiene el item seleccionado en una sección
     * @param sectionName Nombre de la sección
     * @return Item seleccionado o null
     */
    public MenuItem getSelectedItem(String sectionName) {
        PaginationSection section = sections.get(sectionName);
        if (section != null && section.getSelectedIndex() != null) {
            return section.getItem(section.getSelectedIndex());
        }
        return null;
    }
    private Consumer<Player> externalCloseHandler = null;

    @Override
    public MultiPaginationMenu setCloseHandler(Consumer<Player> closeHandler) {
        this.externalCloseHandler = closeHandler;
        super.setCloseHandler(this::onPlayerCloseMenu);
        return this;
    }

    @Override
    public MultiPaginationMenu setReturnMenu(Menu returnMenu) {
        super.setReturnMenu(returnMenu);
        return this;
    }

    @Override
    public MultiPaginationMenu enableDynamicUpdates(JavaPlugin plugin, long tickInterval) {
        super.enableDynamicUpdates(plugin, tickInterval);
        return this;
    }

    @Override
    public MultiPaginationMenu disableDynamicUpdates() {
        super.disableDynamicUpdates();

        for (Map<String, Map<Integer, Integer>> sectionTasks : playerItemTasks.values()) {
            for (Map<Integer, Integer> itemTasks : sectionTasks.values()) {
                for (Integer taskId : itemTasks.values()) {
                    if (taskId != null && taskId != -1) {
                        Bukkit.getScheduler().cancelTask(taskId);
                    }
                }
            }
        }
        playerItemTasks.clear();

        return this;
    }
}