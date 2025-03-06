package net.exylia.exyliaCommons;

import net.exylia.exyliaCommons.utils.ColorUtils;
import net.exylia.exyliaCommons.utils.DebugUtils;
import net.exylia.exyliaCommons.utils.ItemUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExyliaCommons {

    private static JavaPlugin instance;

    private ExyliaCommons() {
    }

    public JavaPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ExyliaCommons no ha sido inicializado. Llama a ExyliaCommons.initialize(plugin) desde el plugin principal.");
        }
        return instance;
    }

    public void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = plugin;
        } else {
            throw new IllegalStateException("ExyliaCommons ya ha sido inicializado.");
        }
    }

    public ColorUtils getColorUtils() {
        return new ColorUtils(instance);
    }

    public DebugUtils getDebugUtils() {
        return new DebugUtils(instance);
    }

    public ItemUtils getItemUtils() {
        return new ItemUtils(instance);
    }
}
