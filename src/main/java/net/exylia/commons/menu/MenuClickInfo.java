package net.exylia.commons.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Información sobre un clic en un ítem de menú
 */
public class MenuClickInfo {
    private final Player player;
    private final ClickType clickType;
    private final int slot;
    private final Menu menu;

    /**
     * Constructor de la información de clic
     * @param player Jugador que hizo clic
     * @param clickType Tipo de clic
     * @param slot Posición del ítem
     * @param menu Menú donde ocurrió el clic
     */
    public MenuClickInfo(Player player, ClickType clickType, int slot, Menu menu) {
        this.player = player;
        this.clickType = clickType;
        this.slot = slot;
        this.menu = menu;
    }

    /**
     * Obtiene el jugador que hizo clic
     * @return Jugador
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Obtiene el tipo de clic
     * @return Tipo de clic
     */
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * Obtiene la posición del ítem
     * @return Posición
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Obtiene el menú donde ocurrió el clic
     * @return Menú
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * Comprueba si el clic fue con botón izquierdo
     * @return true si fue con botón izquierdo
     */
    public boolean isLeftClick() {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }

    /**
     * Comprueba si el clic fue con botón derecho
     * @return true si fue con botón derecho
     */
    public boolean isRightClick() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Comprueba si el clic fue con shift
     * @return true si fue con shift
     */
    public boolean isShiftClick() {
        return clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Comprueba si el clic fue con la rueda del ratón
     * @return true si fue con la rueda
     */
    public boolean isMiddleClick() {
        return clickType == ClickType.MIDDLE;
    }
}