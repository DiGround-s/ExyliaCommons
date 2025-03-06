package net.exylia.exyliaCommons.utils;

import net.exylia.exyliaCommons.ExyliaCommons;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    public static void addCustomNBT(ItemStack item, String keyName, double value) {
        NamespacedKey key = new NamespacedKey(ExyliaCommons.getInstance(), keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }


    public static void addCustomNBT(ItemStack item, String keyName, String value) {
        NamespacedKey key = new NamespacedKey(ExyliaCommons.getInstance(), keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    public static void addCustomNBT(ItemStack item, String keyName, boolean value) {
        NamespacedKey key = new NamespacedKey(ExyliaCommons.getInstance(), keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, (value ? "true" : "false"));
        item.setItemMeta(meta);
    }

    public static String getCustomNBT(ItemStack item, String keyName) {
        NamespacedKey key = new NamespacedKey(ExyliaCommons.getInstance(), keyName);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        if (dataContainer.has(key, PersistentDataType.STRING)) {
            return (String)dataContainer.get(key, PersistentDataType.STRING);
        }
        return null;
    }

    public static boolean isDefaultItem(ItemStack item) {
        return "true".equals(ItemUtils.getCustomNBT(item, "exylia-crystal-default-item"));
    }

    public static List<String> getColoredLore(List<String> lore) {
        ArrayList<String> coloredLore = new ArrayList<String>();
        for (String line : lore) {
            coloredLore.add(ColorUtils.oldTranslateColors(line));
        }
        return coloredLore;
    }
}

