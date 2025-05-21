package net.exylia.commons.utils.versions;

import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.ItemMetaAdapter;
import net.exylia.commons.utils.OldColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemMeta_1_17_Adapter implements ItemMetaAdapter {

    @Override
    public void setDisplayName(ItemMeta meta, Component name) {
        // Convertir Component a String con códigos de color
        String miniMessageString = MiniMessage.miniMessage().serialize(name);
        meta.setDisplayName(OldColorUtils.parseOld(miniMessageString));
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        // Convertir cada Component a String con códigos de color
        List<String> legacyLore = new ArrayList<>();
        for (Component line : lore) {
            Bukkit.getLogger().info("-1. " + lore.toString());
            String miniMessageString = MiniMessage.miniMessage().serialize(line);
            Bukkit.getLogger().info("0. " + miniMessageString);
            legacyLore.add(OldColorUtils.parseOld(miniMessageString));
        }
        meta.setLore(legacyLore);
    }
}