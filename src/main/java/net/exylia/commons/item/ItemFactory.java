package net.exylia.commons.item;

import java.util.Arrays;
import java.util.List;

/**
 * Fábrica para crear ítems interactivos comunes rápidamente
 */
public class ItemFactory {

    /**
     * Crea un ítem básico interactivo
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param lore Descripción del ítem
     * @return Ítem creado
     */
    public static InteractiveItem createItem(String material, String name, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes();
    }

    /**
     * Crea un ítem brillante (con encantamiento oculto)
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param lore Descripción del ítem
     * @return Ítem brillante
     */
    public static InteractiveItem createGlowingItem(String material, String name, String... lore) {
        return createItem(material, name, lore)
                .setGlowing(true);
    }

    /**
     * Crea un ítem con soporte de placeholders
     * @param material Material del ítem
     * @param name Nombre del ítem (admite placeholders)
     * @param lore Descripción del ítem (admite placeholders)
     * @return Ítem con soporte de placeholders
     */
    public static InteractiveItem createPlaceholderItem(String material, String name, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .usePlaceholders(true);
    }

    /**
     * Crea un ítem con comandos a ejecutar al hacer clic
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param commands Lista de comandos a ejecutar
     * @param lore Descripción del ítem
     * @return Ítem con comandos
     */
    public static InteractiveItem createCommandItem(String material, String name, List<String> commands, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .setCommands(commands);
    }

    /**
     * Crea un ítem con comandos a ejecutar al hacer clic
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param commands Array de comandos a ejecutar
     * @return Ítem con comandos
     */
    public static InteractiveItem createCommandItem(String material, String name, String... commands) {
        return new InteractiveItem(material)
                .setName(name)
                .hideAllAttributes()
                .setCommands(Arrays.asList(commands));
    }

    /**
     * Crea un ítem con una acción personalizada
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param action Acción a ejecutar (ej: "show_help", "open_shop gold")
     * @param lore Descripción del ítem
     * @return Ítem con acción
     */
    public static InteractiveItem createActionItem(String material, String name, String action, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .setAction(action);
    }

    /**
     * Crea un ítem con acción y placeholders
     * @param material Material del ítem
     * @param name Nombre del ítem (admite placeholders)
     * @param action Acción a ejecutar
     * @param lore Descripción del ítem (admite placeholders)
     * @return Ítem con acción y placeholders
     */
    public static InteractiveItem createPlaceholderActionItem(String material, String name, String action, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .usePlaceholders(true)
                .setAction(action);
    }

    /**
     * Crea un ítem consumible (se consume al usar)
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param action Acción a ejecutar
     * @param lore Descripción del ítem
     * @return Ítem consumible
     */
    public static InteractiveItem createConsumableItem(String material, String name, String action, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .setAction(action)
                .setConsumeOnUse(true);
    }

    /**
     * Crea un ítem que no cancela eventos (permite comportamiento normal del item)
     * @param material Material del ítem
     * @param name Nombre del ítem
     * @param action Acción a ejecutar
     * @param lore Descripción del ítem
     * @return Ítem que no cancela eventos
     */
    public static InteractiveItem createPassthroughItem(String material, String name, String action, String... lore) {
        return new InteractiveItem(material)
                .setName(name)
                .setLore(lore)
                .hideAllAttributes()
                .setAction(action)
                .setCancelEvent(false);
    }
}