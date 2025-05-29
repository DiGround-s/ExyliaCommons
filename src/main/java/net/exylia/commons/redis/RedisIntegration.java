package net.exylia.commons.redis;

import net.exylia.commons.ExyliaPlugin;
import net.exylia.commons.redis.config.RedisConfig;
import org.bukkit.configuration.file.FileConfiguration;

import static net.exylia.commons.utils.DebugUtils.logInfo;
import static net.exylia.commons.utils.DebugUtils.logError;

/**
 * Clase de integración para configurar Redis automáticamente en ExyliaPlugin
 */
public class RedisIntegration {

    private static boolean autoInitialized = false;

    /**
     * Inicializa Redis automáticamente desde la configuración del plugin
     */
    public static void initializeRedis(ExyliaPlugin plugin) {
        if (autoInitialized) {
            logInfo("Redis ya está inicializado automáticamente");
            return;
        }

        try {
            FileConfiguration config = plugin.getConfig();

            // Verificar si Redis está habilitado en la configuración
            if (!config.getBoolean("redis.enabled", false)) {
                logInfo("Redis está deshabilitado en la configuración");
                return;
            }

            // Crear configuración de Redis desde el archivo
            RedisConfig redisConfig = RedisConfig.fromConfig(config);

            // Inicializar Redis
            RedisManager.initialize(plugin, redisConfig);

            autoInitialized = true;
            logInfo("Redis inicializado automáticamente desde configuración");

        } catch (Exception e) {
            logError("Error al inicializar Redis automáticamente: " + e.getMessage());
        }
    }

    /**
     * Cierra Redis si fue inicializado automáticamente
     */
    public static void shutdownRedis() {
        if (autoInitialized && RedisManager.isAvailable()) {
            try {
                RedisManager.getInstance().shutdown();
                autoInitialized = false;
                logInfo("Redis cerrado automáticamente");
            } catch (Exception e) {
                logError("Error al cerrar Redis automáticamente: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si Redis fue inicializado automáticamente
     */
    public static boolean isAutoInitialized() {
        return autoInitialized;
    }
}
