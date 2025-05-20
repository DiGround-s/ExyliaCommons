package net.exylia.commons.utils.versions;

import net.exylia.commons.utils.ItemMetaAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemMeta_1_18_Adapter implements ItemMetaAdapter {

    @Override
    public void setDisplayName(ItemMeta meta, Component name) {
        meta.displayName(name);
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        meta.lore(lore);
    }
}