package net.exylia.exyliaCommons;

import net.exylia.exyliaCommons.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ExyliaCommons extends JavaPlugin {

    private static ExyliaCommons instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this, List.of("config"));
        getLogger().info("ExyliaCommons ha sido cargado como librería.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ExyliaCommons ha sido deshabilitado.");
    }

    public static ExyliaCommons getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() { return configManager; }
}
