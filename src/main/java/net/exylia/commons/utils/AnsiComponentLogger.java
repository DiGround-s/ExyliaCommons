package net.exylia.commons.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Logger que soporta componentes ANSI para colorear la consola
 */
public class AnsiComponentLogger {
    private static final Pattern HEX_PATTERN = Pattern.compile(
            "§#([0-9a-fA-F]{6})"
    );

    private static final String RGB_ANSI = "\u001B[38;2;%d;%d;%dm";
    private static final String RESET = "\u001B[0m";

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('§')
            .hexCharacter('#')
            .build();

    private static final Function<Component, String> converter =
            supportsAnsi() ? AnsiComponentLogger::convertHexColors : AnsiComponentLogger::stripHexColors;

    /**
     * Convierte un componente a una cadena con colores ANSI
     * @param component El componente de entrada
     * @return Cadena convertida
     */
    public static String convert(final Component component) {
        return converter.apply(component);
    }

    /**
     * Convierte colores hexadecimales a colores ANSI RGB
     * @param input El componente de entrada
     * @return Cadena con colores ANSI RGB
     */
    public static String convertHexColors(final Component input) {
        String serialized = SERIALIZER.serialize(input);
        return HEX_PATTERN.matcher(serialized).replaceAll(result -> {
            final int hex = Integer.decode("0x" + result.group().substring(2));
            final int red = hex >> 16 & 0xFF;
            final int green = hex >> 8 & 0xFF;
            final int blue = hex & 0xFF;
            return String.format(RGB_ANSI, red, green, blue);
        }) + RESET;
    }

    /**
     * Elimina colores hexadecimales si ANSI no es soportado
     * @param input El componente de entrada
     * @return Cadena con colores hexadecimales eliminados
     */
    private static String stripHexColors(final Component input) {
        String serialized = SERIALIZER.serialize(input);
        return HEX_PATTERN.matcher(serialized).replaceAll("");
    }

    /**
     * Comprueba si ANSI es soportado en la terminal actual
     * @return true si ANSI es soportado, false en caso contrario
     */
    private static boolean supportsAnsi() {
        String osName = System.getProperty("os.name").toLowerCase();
        String term = System.getenv("TERM");

        // Verificación básica de soporte ANSI
        return !osName.contains("win") ||
                (term != null && !term.equals("dumb"));
    }

    /**
     * Convierte códigos de color legacy a colores ANSI
     * @param input El componente de entrada
     * @return Cadena con colores ANSI
     */
    public static String convertLegacyColors(final Component input) {
        String serialized = SERIALIZER.serialize(input);
        return convertLegacyColorCodes(serialized);
    }

    /**
     * Método interno para convertir códigos de color legacy
     * @param input La cadena con códigos de color legacy
     * @return Cadena con colores ANSI
     */
    private static String convertLegacyColorCodes(String input) {
        // Mapa de códigos de color legacy a colores ANSI
        return input
                .replace("§0", "\u001B[30m")   // Negro
                .replace("§1", "\u001B[34m")   // Azul Oscuro
                .replace("§2", "\u001B[32m")   // Verde Oscuro
                .replace("§3", "\u001B[36m")   // Turquesa Oscuro
                .replace("§4", "\u001B[31m")   // Rojo Oscuro
                .replace("§5", "\u001B[35m")   // Púrpura Oscuro
                .replace("§6", "\u001B[33m")   // Dorado
                .replace("§7", "\u001B[37m")   // Gris
                .replace("§8", "\u001B[90m")   // Gris Oscuro
                .replace("§9", "\u001B[94m")   // Azul
                .replace("§a", "\u001B[92m")   // Verde
                .replace("§b", "\u001B[96m")   // Turquesa
                .replace("§c", "\u001B[91m")   // Rojo
                .replace("§d", "\u001B[95m")   // Púrpura Claro
                .replace("§e", "\u001B[93m")   // Amarillo
                .replace("§f", "\u001B[97m")   // Blanco
                + RESET;
    }
}