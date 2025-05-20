package net.exylia.commons.utils;

import net.exylia.commons.utils.versions.Inventory_1_17_Adapter;
import net.exylia.commons.utils.versions.Inventory_1_18_Adapter;
import net.exylia.commons.utils.versions.ItemMeta_1_17_Adapter;
import net.exylia.commons.utils.versions.ItemMeta_1_18_Adapter;
import org.bukkit.Bukkit;

public class AdapterFactory {

    private static final ItemMetaAdapter itemMetaAdapter;
    private static final InventoryAdapter inventoryAdapter;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.startsWith("v1_17") || version.startsWith("v1_16_") || version.startsWith("v1_15_") || version.startsWith("v1_14_")) {
            itemMetaAdapter = new ItemMeta_1_17_Adapter();
            inventoryAdapter = new Inventory_1_17_Adapter();
        } else {
            itemMetaAdapter = new ItemMeta_1_18_Adapter();
            inventoryAdapter = new Inventory_1_18_Adapter();
        }
    }

    public static ItemMetaAdapter getItemMetaAdapter() {
        return itemMetaAdapter;
    }

    public static InventoryAdapter getInventoryAdapter() {
        return inventoryAdapter;
    }
}