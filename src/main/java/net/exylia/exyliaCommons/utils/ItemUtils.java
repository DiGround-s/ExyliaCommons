package net.exylia.exyliaCommons.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtils {
    private final JavaPlugin plugin;
    public ItemUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addCustomNBT(ItemStack item, String keyName, double value) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }


    public void addCustomNBT(ItemStack item, String keyName, String value) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    public void addCustomNBT(ItemStack item, String keyName, boolean value) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, (value ? "true" : "false"));
        item.setItemMeta(meta);
    }

    public String getCustomNBT(ItemStack item, String keyName) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
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
}

