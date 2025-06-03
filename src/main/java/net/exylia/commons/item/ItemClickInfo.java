package net.exylia.commons.item;

import net.exylia.commons.actions.ActionSource;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Información sobre un clic en un ítem interactivo
 */
public record ItemClickInfo(Player player, ClickType clickType, int slot, ItemStack itemStack, ActionSource source) {

    /**
     * Obtiene el jugador que hizo clic
     * @return Jugador
     */
    @Override
    public Player player() {
        return player;
    }

    /**
     * Obtiene el tipo de clic
     * @return Tipo de clic
     */
    @Override
    public ClickType clickType() {
        return clickType;
    }

    /**
     * Obtiene la posición del ítem
     * @return Posición
     */
    @Override
    public int slot() {
        return slot;
    }

    /**
     * Obtiene el ItemStack que fue clickeado
     * @return ItemStack
     */
    @Override
    public ItemStack itemStack() {
        return itemStack;
    }

    /**
     * Obtiene la fuente de la acción
     * @return Fuente de la acción
     */
    @Override
    public ActionSource source() {
        return source;
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