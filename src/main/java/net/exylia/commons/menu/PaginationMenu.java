package net.exylia.commons.menu;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

        // Botones de navegación por defecto
        this.previousPageButton = new MenuItem(Material.ARROW)
                .setName("&8« &7Página anterior")
                .setAmount(1);

        this.nextPageButton = new MenuItem(Material.ARROW)
                .setName("&7Página siguiente &8»")
                .setAmount(1);

        // Posiciones de botones por defecto
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
        int maxPages = (int) Math.ceil((double) items.size() / maxItemsPerPage);
        maxPages = Math.max(1, maxPages);

        // Validar la página solicitada
        if (page < 1) {
            page = 1;
        } else if (page > maxPages) {
            page = maxPages;
        }

        currentPage = page;

        // Crear el menú para la página actual
        Menu menu = createMenuForPage(player, page, maxPages);
        menu.open(player);
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
        // Título con la página actual y total
        String title = baseTitle + " &8(&7" + page + "&8/&7" + maxPages + "&8)";

        // Procesar placeholders en el título si está activado
        if (usePlaceholdersInTitle && MenuManager.isPlaceholderAPIEnabled()) {
            title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, title);
        }

        Menu menu = new Menu(title, rows);

        // Botones de navegación
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

        // Rellenar con ítems
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

        // Aplicar relleno si se ha configurado
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

        // Aplicar personalizador si se ha configurado
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

        if (menuCustomizer != null) {
            clone.setMenuCustomizer(menuCustomizer);
        }

        return clone;
    }
}