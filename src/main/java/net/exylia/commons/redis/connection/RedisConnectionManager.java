package net.exylia.commons.redis.connection;

import net.exylia.commons.redis.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import static net.exylia.commons.utils.DebugUtils.logError;
import static net.exylia.commons.utils.DebugUtils.logInfo;

/**
 * Gestor de conexiones para Redis usando pool de conexiones
 */
public class RedisConnectionManager {

    private final RedisConfig config;
    private JedisPool jedisPool;
    private volatile boolean initialized = false;

    public RedisConnectionManager(RedisConfig config) {
        this.config = config;
    }

    /**
     * Inicializa el pool de conexiones
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            config.validate();

            JedisPoolConfig poolConfig = createPoolConfig();

            if (config.getPassword() != null && !config.getPassword().trim().isEmpty()) {
                jedisPool = new JedisPool(
                        poolConfig,
                        config.getHost(),
                        config.getPort(),
                        config.getTimeout(),
                        config.getPassword(),
                        config.getDatabase(),
                        config.isSsl()
                );
            } else {
                jedisPool = new JedisPool(
                        poolConfig,
                        config.getHost(),
                        config.getPort(),
                        config.getTimeout(),
                        null,
                        config.getDatabase(),
                        config.isSsl()
                );
            }

            // Probar la conexión
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }

            initialized = true;
            logInfo("Pool de conexiones Redis inicializado - Host: " + config.getHost() + ":" + config.getPort());

        } catch (Exception e) {
            logError("Error al inicializar pool de conexiones Redis: " + e.getMessage());
            if (jedisPool != null) {
                jedisPool.close();
                jedisPool = null;
            }
            throw new RuntimeException("No se pudo conectar a Redis", e);
        }
    }

    /**
     * Obtiene una conexión del pool
     */
    public Jedis getConnection() {
        if (!initialized || jedisPool == null) {
            throw new IllegalStateException("ConnectionManager no está inicializado");
        }

        try {
            return jedisPool.getResource();
        } catch (JedisException e) {
            logError("Error al obtener conexión Redis: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si el pool está activo
     */
    public boolean isActive() {
        return initialized && jedisPool != null && !jedisPool.isClosed();
    }

    /**
     * Obtiene estadísticas del pool
     */
    public PoolStats getPoolStats() {
        if (!isActive()) {
            return new PoolStats(0, 0, 0);
        }

        return new PoolStats(
                jedisPool.getNumActive(),
                jedisPool.getNumIdle(),
                jedisPool.getNumWaiters()
        );
    }

    /**
     * Valida las conexiones del pool
     */
    public void validateConnections() {
        if (!isActive()) {
            return;
        }

        try (Jedis jedis = getConnection()) {
            jedis.ping();
        } catch (Exception e) {
            logError("Error validando conexiones Redis: " + e.getMessage());
            // Intentar reinicializar si hay problemas
            reinitialize();
        }
    }

    /**
     * Reinicializa el pool de conexiones
     */
    public synchronized void reinitialize() {
        logInfo("Reinicializando pool de conexiones Redis...");

        shutdown();

        try {
            Thread.sleep(1000); // Esperar un segundo antes de reintentar
            initialize();
            logInfo("Pool de conexiones Redis reinicializado correctamente");
        } catch (Exception e) {
            logError("Error al reinicializar pool Redis: " + e.getMessage());
        }
    }

    /**
     * Cierra el pool de conexiones
     */
    public synchronized void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            try {
                jedisPool.close();
                logInfo("Pool de conexiones Redis cerrado");
            } catch (Exception e) {
                logError("Error al cerrar pool Redis: " + e.getMessage());
            }
        }

        jedisPool = null;
        initialized = false;
    }

    /**
     * Crea la configuración del pool de conexiones
     */
    private JedisPoolConfig createPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // Configuración básica del pool
        poolConfig.setMaxTotal(config.getMaxTotal());
        poolConfig.setMaxIdle(config.getMaxIdle());
        poolConfig.setMinIdle(config.getMinIdle());
        poolConfig.setMaxWaitMillis(config.getMaxWaitMillis());

        // Configuración de validación
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());
        poolConfig.setTestOnReturn(config.isTestOnReturn());
        poolConfig.setTestWhileIdle(config.isTestWhileIdle());
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());

        // Configuración adicional para optimización
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setJmxEnabled(false);

        return poolConfig;
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Estadísticas del pool de conexiones
     */
    public static class PoolStats {
        private final int active;
        private final int idle;
        private final int waiters;

        public PoolStats(int active, int idle, int waiters) {
            this.active = active;
            this.idle = idle;
            this.waiters = waiters;
        }

        public int getActive() {
            return active;
        }

        public int getIdle() {
            return idle;
        }

        public int getWaiters() {
            return waiters;
        }

        public int getTotal() {
            return active + idle;
        }

        @Override
        public String toString() {
            return "PoolStats{" +
                    "active=" + active +
                    ", idle=" + idle +
                    ", waiters=" + waiters +
                    ", total=" + getTotal() +
                    '}';
        }
    }

    // ==================== GETTERS ====================

    public RedisConfig getConfig() {
        return config;
    }

    public boolean isInitialized() {
        return initialized;
    }
}