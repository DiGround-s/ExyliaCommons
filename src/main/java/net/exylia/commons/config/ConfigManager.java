package net.exylia.commons.config;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de configuraciones para plugins de Exylia
 * Permite administrar múltiples archivos de configuración YAML
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private String prefix = "";

    /**
     * Constructor del ConfigManager
     * @param plugin El plugin que utiliza esta configuración
     * @param files Lista de nombres de archivo (sin extensión) a cargar
     */
    public ConfigManager(JavaPlugin plugin, List<String> files) {
        this.plugin = plugin;
        for (String file : files) {
            loadConfig(file);
        }
        if (configs.containsKey("messages")) {
            this.prefix = getConfig("messages").getString("prefix", "");
        }
    }

    /**
     * Carga un archivo de configuración
     * @param fileName Nombre del archivo sin extensión
     */
    private void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            plugin.saveResource(fileName + ".yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
    }

    /**
     * Obtiene un mensaje personalizado
     * @param path La ruta del mensaje en el archivo de mensajes
     * @param replacements Los reemplazos de placeholders
     * @return El componente del mensaje personalizado
     */
    public Component getMessage(String path, String... replacements) {
        String message = getConfig("messages").getString(path, "<#a33b53>" + path + " not found in messages.yml");

        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        message = applyPrefix(message);
        return ColorUtils.parse(message);
    }

    /**
     * Obtiene un mensaje personalizado
     * @param path La ruta del mensaje en el archivo de mensajes
     * @return El componente del mensaje personalizado
     */
    public Component getMessage(String path) {
        String message = getConfig("messages").getString(path, "<#a33b53>" + path + " not found in messages.yml");
        message = applyPrefix(message);
        return ColorUtils.parse(message);
    }
    /**
     * Aplica placeholders al mensaje
     * @param message El mensaje original
     * @return El mensaje con placeholders reemplazados
     */
    private String applyPrefix(String message) {
        return message.replace("%prefix%", prefix);
    }

    /**
     * Obtiene una configuración por nombre de archivo
     * @param fileName Nombre del archivo sin extensión
     * @return El objeto FileConfiguration correspondiente
     */
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }

    /**
     * Obtiene la configuración principal
     * @return El objeto FileConfiguration correspondiente
     */
    public FileConfiguration getConfig() {
        return getConfig("config");
    }

    /**
     * Recarga una configuración específica
     * @param fileName Nombre del archivo sin extensión
     */
    public void reloadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
    }

    /**
     * Recarga todas las configuraciones
     */
    public void reloadAllConfigs() {
        for (String fileName : configs.keySet()) {
            reloadConfig(fileName);
        }
        if (configs.containsKey("messages")) {
            this.prefix = getConfig("messages").getString("prefix", "");
        }
    }
}