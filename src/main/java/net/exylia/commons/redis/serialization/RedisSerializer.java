package net.exylia.commons.redis.serialization;

/**
 * Interfaz para serialización de objetos en Redis
 */
public interface RedisSerializer {

    /**
     * Serializa un objeto a String
     */
    <T> String serialize(T object);

    /**
     * Deserializa un String a objeto
     */
    <T> T deserialize(String data, Class<T> type);

    /**
     * Verifica si puede serializar el tipo dado
     */
    default boolean canSerialize(Class<?> type) {
        return true;
    }
}