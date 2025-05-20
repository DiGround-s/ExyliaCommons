package net.exylia.commons.utils.versions;

import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.InventoryAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class Inventory_1_17_Adapter implements InventoryAdapter {

    @Override
    public Inventory createInventory(int size, Component title) {
        // Convertir Component a String con códigos de color
        String miniMessageString = MiniMessage.miniMessage().serialize(title);
        return Bukkit.createInventory(null, size, ColorUtils.oldTranslateColors(miniMessageString));
    }
}