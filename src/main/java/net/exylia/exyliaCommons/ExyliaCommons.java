package net.exylia.exyliaCommons;

import org.bukkit.plugin.java.JavaPlugin;

public final class ExyliaCommons extends JavaPlugin{
    private static ExyliaCommons instance;
    private JavaPlugin plugin;

    private ExyliaCommons(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static ExyliaCommons getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ExyliaCommons no ha sido inicializado.");
        }
        return instance;
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ExyliaCommons(plugin);
        } else {
            throw new IllegalStateException("ExyliaCommons ya ha sido inicializado.");
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
