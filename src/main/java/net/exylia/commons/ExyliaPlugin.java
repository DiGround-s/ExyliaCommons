package net.exylia.commons;

import net.exylia.commons.menu.MenuManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class ExyliaPlugin extends JavaPlugin {
    private static boolean initialized = false;
    private static final Set<ExyliaPlugin> registeredPlugins = new HashSet<>();
    private BukkitAudiences adventure;
    private static ExyliaPlugin instance;

    @Override
    public final void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        registeredPlugins.add(this);

        if (!initialized) {
            initializeExylia();
            instance = this;
            initialized = true;
        }

        onExyliaEnable();

        getLogger().info("Plugin Exylia habilitado correctamente: " + getDescription().getName());
    }

    @Override
    public final void onDisable() {
        registeredPlugins.remove(this);

        onExyliaDisable();

        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        if (registeredPlugins.isEmpty()) {
            shutdownExylia();
            initialized = false;
        }

        getLogger().info("Plugin Exylia deshabilitado: " + getDescription().getName());
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Attempted to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public BukkitAudiences getAudience() {
        return adventure();
    }

    private void initializeExylia() {
        MenuManager.initialize(this);

        checkOptionalDependencies();

        getLogger().info("Núcleo Exylia inicializado correctamente");
    }

    /**
     * Comprueba y registra las dependencias opcionales
     */
    private void checkOptionalDependencies() {
        // Ejemplo
//        boolean vaultEnabled = Bukkit.getPluginManager().getPlugin("Vault") != null;
//        if (vaultEnabled) {
//            getLogger().info("Vault detectado. Soporte de economía activado.");
//        }
    }

    /**
     * Limpia los recursos globales de Exylia cuando todos los plugins son deshabilitados.
     */
    private void shutdownExylia() {
        getLogger().info("Limpiando recursos globales de Exylia");
    }

    /**
     * Obtiene la instancia de un plugin Exylia registrado por su clase.
     *
     * @param pluginClass La clase del plugin que se desea obtener
     * @return La instancia del plugin o null si no está registrado
     */
    @SuppressWarnings("unchecked")
    public static <T extends ExyliaPlugin> T getExyliaPlugin(Class<T> pluginClass) {
        for (ExyliaPlugin plugin : registeredPlugins) {
            if (pluginClass.isInstance(plugin)) {
                return (T) plugin;
            }
        }
        return null;
    }

    public static ExyliaPlugin getInstance() {
        return instance;
    }

    protected abstract void onExyliaEnable();

    protected abstract void onExyliaDisable();
}