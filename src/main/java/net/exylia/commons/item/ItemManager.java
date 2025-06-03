package net.exylia.commons.item;

import net.exylia.commons.actions.ActionSource;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.exylia.commons.utils.DebugUtils.logInfo;

/**
 * Sistema de gestión de ítems interactivos
 */
public class ItemManager implements Listener {
    private static JavaPlugin plugin;
    private static final Map<String, InteractiveItem> registeredItems = new HashMap<>();
    private static boolean initialized = false;
    private static NamespacedKey itemIdKey;

    /**
     * Inicializa el sistema de ítems
     * @param javaPlugin El plugin principal
     */
    public static void initialize(JavaPlugin javaPlugin) {
        if (initialized) return;
        plugin = javaPlugin;
        itemIdKey = new NamespacedKey(plugin, "interactive_item_id");
        Bukkit.getPluginManager().registerEvents(new ItemManager(), plugin);
        initialized = true;
    }

    /**
     * Registra un ítem interactivo
     * @param item Ítem a registrar
     * @return ItemStack preparado para dar al jugador
     */
    public static ItemStack registerItem(InteractiveItem item) {
        registeredItems.put(item.getId(), item);

        ItemStack itemStack = item.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();

        // Marcar el ítem con su ID para poder identificarlo después
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, item.getId());
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * Desregistra un ítem interactivo
     * @param itemId ID del ítem a desregistrar
     */
    public static void unregisterItem(String itemId) {
        registeredItems.remove(itemId);
    }

    /**
     * Obtiene un ítem interactivo registrado
     * @param itemId ID del ítem
     * @return Ítem interactivo o null si no existe
     */
    public static InteractiveItem getItem(String itemId) {
        return registeredItems.get(itemId);
    }

    /**
     * Obtiene un ítem interactivo desde un ItemStack
     * @param itemStack ItemStack a verificar
     * @return Ítem interactivo o null si no es interactivo
     */
    public static InteractiveItem getItemFromStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        ItemMeta meta = itemStack.getItemMeta();
        String itemId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);

        if (itemId != null) {
            return registeredItems.get(itemId);
        }

        return null;
    }

    /**
     * Verifica si un ItemStack es un ítem interactivo
     * @param itemStack ItemStack a verificar
     * @return true si es interactivo
     */
    public static boolean isInteractiveItem(ItemStack itemStack) {
        return getItemFromStack(itemStack) != null;
    }

    /**
     * Maneja las interacciones con ítems
     * @param event Evento de interacción
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack == null) return;

        InteractiveItem interactiveItem = getItemFromStack(itemStack);
        if (interactiveItem == null) return;

        // Verificar si el ítem tiene usos restantes
        if (!interactiveItem.hasUsesRemaining()) {
            player.sendMessage("§cEste ítem ya no tiene usos restantes.");
            event.setCancelled(true);
            return;
        }

        // Determinar el tipo de acción
        ActionSource source = ActionSource.ITEM_USE;
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            source = ActionSource.ITEM_CLICK;
        }

        // Crear información del clic
        ItemClickInfo clickInfo = new ItemClickInfo(
                player,
                event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK
                        ? org.bukkit.event.inventory.ClickType.LEFT
                        : org.bukkit.event.inventory.ClickType.RIGHT,
                player.getInventory().getHeldItemSlot(),
                itemStack,
                source
        );

        // Cancelar evento si está configurado
        if (interactiveItem.shouldCancelEvent()) {
            event.setCancelled(true);
        }

        // Ejecutar acción personalizada si existe
        boolean actionExecuted = false;
        if (interactiveItem.hasAction()) {
            actionExecuted = interactiveItem.executeAction(clickInfo);
        }

        // Ejecutar comandos si hay definidos y no se ejecutó una acción
        if (!actionExecuted && !interactiveItem.getCommands().isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                interactiveItem.executeCommands(player);
            });
        }

        // Ejecutar el handler de clic si está definido
        if (interactiveItem.getClickHandler() != null) {
            interactiveItem.getClickHandler().accept(clickInfo);
        }

        // Consumir uso si la acción fue exitosa
        if (actionExecuted || !interactiveItem.getCommands().isEmpty() || interactiveItem.getClickHandler() != null) {
            if (!interactiveItem.consumeUse()) {
                // El ítem se quedó sin usos, removerlo
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                }
                player.sendMessage("§7El ítem se ha agotado.");
                return;
            }

            // Actualizar el ítem en el inventario con los nuevos usos
            if (interactiveItem.hasLimitedUses()) {
                ItemStack updatedStack = registerItem(interactiveItem);
                updatedStack.setAmount(itemStack.getAmount());
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), updatedStack);
            }
        }

        // Consumir ítem completo si está configurado para consumirse
        if (interactiveItem.shouldConsumeOnUse()) {
            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
            }
        }
    }

    /**
     * Maneja los clics en inventarios (para ítems en inventarios)
     * @param event Evento de clic en inventario
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        InteractiveItem interactiveItem = getItemFromStack(clickedItem);
        if (interactiveItem == null) return;

        // Verificar si el ítem tiene usos restantes
        if (!interactiveItem.hasUsesRemaining()) {
            player.sendMessage("§cEste ítem ya no tiene usos restantes.");
            event.setCancelled(true);
            return;
        }

        // Crear información del clic
        ItemClickInfo clickInfo = new ItemClickInfo(
                player,
                event.getClick(),
                event.getSlot(),
                clickedItem,
                ActionSource.ITEM_CLICK
        );

        // Cancelar evento si está configurado
        if (interactiveItem.shouldCancelEvent()) {
            event.setCancelled(true);
        }

        // Ejecutar acción personalizada si existe
        boolean actionExecuted = false;
        if (interactiveItem.hasAction()) {
            actionExecuted = interactiveItem.executeAction(clickInfo);
        }

        // Ejecutar comandos si hay definidos y no se ejecutó una acción
        if (!actionExecuted && !interactiveItem.getCommands().isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                interactiveItem.executeCommands(player);
            });
        }

        // Ejecutar el handler de clic si está definido
        if (interactiveItem.getClickHandler() != null) {
            interactiveItem.getClickHandler().accept(clickInfo);
        }

        // Consumir uso si la acción fue exitosa
        if (actionExecuted || !interactiveItem.getCommands().isEmpty() || interactiveItem.getClickHandler() != null) {
            if (!interactiveItem.consumeUse()) {
                // El ítem se quedó sin usos, removerlo
                event.setCurrentItem(null);
                player.sendMessage("§7El ítem se ha agotado.");
                return;
            }

            // Actualizar el ítem en el inventario con los nuevos usos
            if (interactiveItem.hasLimitedUses()) {
                ItemStack updatedStack = registerItem(interactiveItem);
                updatedStack.setAmount(clickedItem.getAmount());
                event.setCurrentItem(updatedStack);
            }
        }
    }

    /**
     * Obtiene el plugin asociado
     * @return Plugin principal
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Limpia todos los ítems registrados
     */
    public static void clearAllItems() {
        registeredItems.clear();
    }

    /**
     * Obtiene todos los ítems registrados
     * @return Mapa de ítems registrados
     */
    public static Map<String, InteractiveItem> getRegisteredItems() {
        return new HashMap<>(registeredItems);
    }
}