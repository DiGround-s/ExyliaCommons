package net.exylia.commons.utils.versions;

import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.ItemMetaAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemMeta_1_17_Adapter implements ItemMetaAdapter {

    @Override
    public void setDisplayName(ItemMeta meta, Component name) {
        // Convertir Component a String con códigos de color
        String miniMessageString = MiniMessage.miniMessage().serialize(name);
        meta.setDisplayName(ColorUtils.oldTranslateColors(miniMessageString));
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        // Convertir cada Component a String con códigos de color
        List<String> legacyLore = new ArrayList<>();
        for (Component line : lore) {
            String miniMessageString = MiniMessage.miniMessage().serialize(line);
            legacyLore.add(ColorUtils.oldTranslateColors(miniMessageString));
        }
        meta.setLore(legacyLore);
    }
}