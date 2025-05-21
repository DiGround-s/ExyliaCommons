package net.exylia.commons.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Sistema de cache genérico optimizado para aplicaciones de alto rendimiento
 * con soporte para expiración automática y carga asincrónica
 *
 * @param <K> Tipo de la clave
 * @param <V> Tipo del valor
 */
public class Cache<K, V> {

    private final Map<K, CacheEntry<V>> cacheMap;
    private final long defaultExpirationMs;
    private final int maxSize;
    private final ScheduledExecutorService cleanupService;

    /**
     * Constructor para crear un cache con configuración personalizada
     *
     * @param defaultExpirationMs Tiempo de expiración predeterminado en milisegundos
     * @param maxSize Tamaño máximo del cache, 0 para ilimitado
     * @param cleanupIntervalMs Intervalo para limpiar entradas expiradas
     */
    public Cache(long defaultExpirationMs, int maxSize, long cleanupIntervalMs) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.defaultExpirationMs = defaultExpirationMs;
        this.maxSize = maxSize;

        // Programar limpieza periódica si se especifica un intervalo
        if (cleanupIntervalMs > 0) {
            this.cleanupService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "Cache-Cleanup-Thread");
                thread.setDaemon(true);
                return thread;
            });

            this.cleanupService.scheduleAtFixedRate(
                    this::cleanup,
                    cleanupIntervalMs,
                    cleanupIntervalMs,
                    TimeUnit.MILLISECONDS
            );
        } else {
            this.cleanupService = null;
        }
    }

    /**
     * Constructor con valores predeterminados recomendados
     * Expiración: 10 minutos, sin límite de tamaño, limpieza cada 5 minutos
     */
    public Cache() {
        this(600000, 0, 300000);
    }

    /**
     * Obtiene un valor del cache, o lo carga usando la función proporcionada si no existe
     *
     * @param key La clave para buscar
     * @param loadFunction Función para cargar el valor si no existe en cache
     * @return El valor almacenado o recién cargado
     */
    public V get(K key, Function<K, V> loadFunction) {
        CacheEntry<V> entry = cacheMap.get(key);

        // Si la entrada existe y no ha expirado
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        // Cargar el valor usando la función proporcionada
        V value = loadFunction.apply(key);

        // Almacenar el nuevo valor con el tiempo de expiración predeterminado
        put(key, value);

        return value;
    }

    /**
     * Almacena un valor en el cache con tiempo de expiración predeterminado
     *
     * @param key La clave
     * @param value El valor a almacenar
     */
    public void put(K key, V value) {
        put(key, value, defaultExpirationMs);
    }

    /**
     * Almacena un valor en el cache con tiempo de expiración personalizado
     *
     * @param key La clave
     * @param value El valor a almacenar
     * @param expirationMs Tiempo de expiración en milisegundos, 0 para no expirar
     */
    public void put(K key, V value, long expirationMs) {
        // Controlar tamaño máximo si está configurado
        if (maxSize > 0 && cacheMap.size() >= maxSize && !cacheMap.containsKey(key)) {
            evictOldest();
        }

        long expiration = expirationMs > 0 ? System.currentTimeMillis() + expirationMs : 0;
        cacheMap.put(key, new CacheEntry<>(value, expiration));
    }

    /**
     * Elimina una entrada del cache
     *
     * @param key La clave a eliminar
     * @return true si se eliminó la entrada, false si no existía
     */
    public boolean remove(K key) {
        return cacheMap.remove(key) != null;
    }

    /**
     * Verifica si una clave existe en el cache y no ha expirado
     *
     * @param key La clave a verificar
     * @return true si la clave existe y no ha expirado
     */
    public boolean contains(K key) {
        CacheEntry<V> entry = cacheMap.get(key);
        return entry != null && !entry.isExpired();
    }

    /**
     * Limpia el cache
     */
    public void clear() {
        cacheMap.clear();
    }

    /**
     * Obtiene el número de entradas en el cache
     *
     * @return Número de entradas
     */
    public int size() {
        return cacheMap.size();
    }

    /**
     * Cierra recursos asociados al cache
     */
    public void shutdown() {
        if (cleanupService != null) {
            cleanupService.shutdown();
        }
    }

    /**
     * Elimina entradas expiradas del cache
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        cacheMap.entrySet().removeIf(entry -> {
            CacheEntry<V> cacheEntry = entry.getValue();
            return cacheEntry.getExpirationTime() > 0 && cacheEntry.getExpirationTime() <= now;
        });
    }

    /**
     * Desaloja la entrada más antigua cuando se alcanza el tamaño máximo
     * Esta implementación simple elimina una entrada aleatoria, pero podría
     * mejorarse con políticas LRU u otras estrategias
     */
    private void evictOldest() {
        if (!cacheMap.isEmpty()) {
            K firstKey = cacheMap.keySet().iterator().next();
            cacheMap.remove(firstKey);
        }
    }

    /**
     * Clase interna para almacenar valores en cache con metadatos
     *
     * @param <V> Tipo del valor
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long expirationTime;

        public CacheEntry(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public V getValue() {
            return value;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isExpired() {
            return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
        }
    }
}