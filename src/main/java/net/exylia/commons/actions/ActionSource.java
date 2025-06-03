package net.exylia.commons.actions;

/**
 * Fuente de donde proviene una acción
 */
public enum ActionSource {
    MENU,           // Acción desde un menú
    ITEM_CLICK,     // Acción desde clic en item
    ITEM_USE,       // Acción desde uso de item
    COMMAND,        // Acción desde comando
    NPC,            // Acción desde NPC
    CUSTOM          // Acción personalizada
}