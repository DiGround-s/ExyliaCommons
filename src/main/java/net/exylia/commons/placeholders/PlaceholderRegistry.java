package net.exylia.commons.placeholders;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PlaceholderRegistry {

    // Placeholders que solo necesitan el contexto
    private static final Map<String, Function<Object, Object>> contextPlaceholders = new HashMap<>();

    // Placeholders que necesitan el contexto y el jugador
    private static final Map<String, BiFunction<Object, Player, Object>> playerContextPlaceholders = new HashMap<>();

    // Placeholders que solo necesitan el jugador
    private static final Map<String, Function<Player, Object>> playerPlaceholders = new HashMap<>();

    /**
     * Registra un placeholder que solo usa el contexto
     * @param placeholder Nombre del placeholder (sin %)
     * @param replacer Función que recibe el contexto y devuelve el valor (String, Component o cualquier objeto)
     */
    public static void registerContext(String placeholder, Function<Object, Object> replacer) {
        contextPlaceholders.put(placeholder, replacer);
    }

    /**
     * Registra un placeholder que usa el contexto y el jugador
     * @param placeholder Nombre del placeholder (sin %)
     * @param replacer Función que recibe el contexto y el jugador, devuelve el valor (String, Component o cualquier objeto)
     */
    public static void registerPlayerContext(String placeholder, BiFunction<Object, Player, Object> replacer) {
        playerContextPlaceholders.put(placeholder, replacer);
    }

    /**
     * Registra un placeholder que solo usa el jugador
     * @param placeholder Nombre del placeholder (sin %)
     * @param replacer Función que recibe el jugador y devuelve el valor (String, Component o cualquier objeto)
     */
    public static void registerPlayer(String placeholder, Function<Player, Object> replacer) {
        playerPlaceholders.put(placeholder, replacer);
    }

    /**
     * Convierte un objeto a String manejando Components de Kyori
     * @param obj Objeto a convertir (puede ser String, Component o cualquier otro objeto)
     * @return String representation del objeto
     */
    private static String objectToString(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof Component component) {
            return PlainTextComponentSerializer.plainText().serialize(component);
        }

        if (obj instanceof String string) {
            return string;
        }

        return obj.toString();
    }

    /**
     * Procesa todos los placeholders en un texto
     * @param text Texto con placeholders
     * @param context Contexto (puede ser null)
     * @param player Jugador (puede ser null)
     * @return Texto con placeholders procesados
     */
    public static String process(String text, Object context, Player player) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // Procesar placeholders de contexto
        for (Map.Entry<String, Function<Object, Object>> entry : contextPlaceholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            if (result.contains(placeholder)) {
                try {
                    Object replacement = entry.getValue().apply(context);
                    result = result.replace(placeholder, objectToString(replacement));
                } catch (Exception e) {
                    // En caso de error, mantener el placeholder original
                    continue;
                }
            }
        }

        // Procesar placeholders de jugador + contexto
        for (Map.Entry<String, BiFunction<Object, Player, Object>> entry : playerContextPlaceholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            if (result.contains(placeholder)) {
                try {
                    Object replacement = entry.getValue().apply(context, player);
                    result = result.replace(placeholder, objectToString(replacement));
                } catch (Exception e) {
                    // En caso de error, mantener el placeholder original
                    continue;
                }
            }
        }

        // Procesar placeholders de jugador
        for (Map.Entry<String, Function<Player, Object>> entry : playerPlaceholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            if (result.contains(placeholder)) {
                try {
                    Object replacement = entry.getValue().apply(player);
                    result = result.replace(placeholder, objectToString(replacement));
                } catch (Exception e) {
                    // En caso de error, mantener el placeholder original
                    continue;
                }
            }
        }

        return result;
    }

    /**
     * Limpia todos los placeholders registrados
     */
    public static void clear() {
        contextPlaceholders.clear();
        playerContextPlaceholders.clear();
        playerPlaceholders.clear();
    }

    /**
     * Obtiene estadísticas de placeholders registrados
     */
    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("context", contextPlaceholders.size());
        stats.put("playerContext", playerContextPlaceholders.size());
        stats.put("player", playerPlaceholders.size());
        stats.put("total", contextPlaceholders.size() + playerContextPlaceholders.size() + playerPlaceholders.size());
        return stats;
    }
}