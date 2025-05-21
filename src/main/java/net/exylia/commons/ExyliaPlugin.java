package net.exylia.commons;

import net.exylia.commons.menu.MenuManager;
import net.exylia.commons.utils.AdapterFactory;
import net.exylia.commons.utils.Cache;
import net.exylia.commons.utils.ColorUtils;
import net.exylia.commons.utils.OldColorUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import static net.exylia.commons.utils.DebugUtils.logInfo;

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

        logInfo("Plugin Exylia habilitado correctamente: " + getDescription().getName());
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

        logInfo("Plugin Exylia deshabilitado: " + getDescription().getName());
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
        AdapterFactory.initialize(this);

        checkOptionalDependencies();

        logInfo("Núcleo Exylia inicializado correctamente");
    }

    private void checkOptionalDependencies() {
        // Ejemplo
//        boolean vaultEnabled = Bukkit.getPluginManager().getPlugin("Vault") != null;
//        if (vaultEnabled) {
//            getLogger().info("Vault detectado. Soporte de economía activado.");
//        }
    }

    private void shutdownExylia() {
        logInfo("Limpiando recursos globales de Exylia");
        ColorUtils.shutdown();
        OldColorUtils.shutdown();
        AdapterFactory.close();
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