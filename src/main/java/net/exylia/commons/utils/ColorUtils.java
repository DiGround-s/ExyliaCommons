package net.exylia.commons.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Utilidades optimizadas para manejar colores y componentes de texto
 * utilizando MiniMessage con caché para mejorar el rendimiento
 */
public class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Cache<String, Component> COMPONENT_CACHE = new Cache<>(1800000, 500, 300000);

    /**
     * Traduce códigos de color a componentes Adventure con caché
     *
     * @param message Mensaje con códigos de color
     * @return Componente con colores y formato aplicados
     */
    public static Component parse(String message) {
        if (message == null) {
            return Component.empty();
        }

        // Usar cache para evitar reprocesamiento
        return COMPONENT_CACHE.get(message, key -> {
            String processed = preprocessColorCodes(key);
            return MINI_MESSAGE.deserialize(processed)
                    .decoration(TextDecoration.ITALIC, false);
        });
    }

    /**
     * Traduce códigos de color a componentes Adventure de forma asíncrona
     * @param message Mensaje con códigos de color
     * @return CompletableFuture con el componente procesado
     */
    public static CompletableFuture<Component> parseAsync(String message) {
        return CompletableFuture.supplyAsync(() -> parse(message));
    }

    /**
     * Traduce una lista de mensajes a componentes
     * @param messages Lista de mensajes con códigos de color
     * @return Lista de componentes procesados
     */
    public static List<Component> parse(List<String> messages) {
        return messages.stream()
                .map(ColorUtils::parse)
                .collect(Collectors.toList());
    }

    /**
     * Traduce una lista de mensajes asíncronamente
     * @param messages Lista de mensajes con códigos de color
     * @return CompletableFuture con la lista de componentes
     */
    public static CompletableFuture<List<Component>> parseAsync(List<String> messages) {
        return CompletableFuture.supplyAsync(() -> parse(messages));
    }

    /**
     * Preprocesa códigos de color ampersand (&) a formato MiniMessage
     * @param message Mensaje con códigos de color
     * @return Mensaje con códigos convertidos a formato MiniMessage
     */
    private static String preprocessColorCodes(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        message = message.replace('§', '&');

        // Convertir códigos &# a <#hexcode>
        message = message.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");

        // Convertir códigos simples &x a sus equivalentes MiniMessage
        message = message.replace("&0", "<black>");
        message = message.replace("&1", "<dark_blue>");
        message = message.replace("&2", "<dark_green>");
        message = message.replace("&3", "<dark_aqua>");
        message = message.replace("&4", "<dark_red>");
        message = message.replace("&5", "<dark_purple>");
        message = message.replace("&6", "<gold>");
        message = message.replace("&7", "<gray>");
        message = message.replace("&8", "<dark_gray>");
        message = message.replace("&9", "<blue>");
        message = message.replace("&a", "<green>");
        message = message.replace("&b", "<aqua>");
        message = message.replace("&c", "<red>");
        message = message.replace("&d", "<light_purple>");
        message = message.replace("&e", "<yellow>");
        message = message.replace("&f", "<white>");

        // Convertir códigos de formato
        message = message.replace("&k", "<obfuscated>");
        message = message.replace("&l", "<bold>");
        message = message.replace("&m", "<strikethrough>");
        message = message.replace("&n", "<underlined>");
        message = message.replace("&o", "<italic>");
        message = message.replace("&r", "<reset>");

        return message;
    }

    /**
     * Normaliza un código de color a formato MiniMessage
     * @param input Código de color en cualquier formato soportado
     * @return Código de color en formato MiniMessage
     */
    public static String normalizeColor(String input) {
        if (input == null || input.isBlank()) {
            return "<#ffffff>";
        }

        input = input.trim().toLowerCase();

        // Si ya está en formato MiniMessage <#ffffff>
        if (input.matches("<#[0-9a-f]{6}>")) {
            return input;
        }

        // Si es un código hexadecimal en varios formatos (ffffff, #ffffff, &#ffffff)
        if (input.matches("#?[0-9a-f]{6}") || input.matches("&#[0-9a-f]{6}")) {
            return "<#" + input.replace("#", "").replace("&", "") + ">";
        }

        // Si es un código de color con "&" (ej: &f)
        return switch (input) {
            case "&0" -> "<black>";
            case "&1" -> "<dark_blue>";
            case "&2" -> "<dark_green>";
            case "&3" -> "<dark_aqua>";
            case "&4" -> "<dark_red>";
            case "&5" -> "<dark_purple>";
            case "&6" -> "<gold>";
            case "&7" -> "<gray>";
            case "&8" -> "<dark_gray>";
            case "&9" -> "<blue>";
            case "&a" -> "<green>";
            case "&b" -> "<aqua>";
            case "&c" -> "<red>";
            case "&d" -> "<light_purple>";
            case "&e" -> "<yellow>";
            case "&f" -> "<white>";
            default -> "<#ffffff>";
        };
    }

    public static void clearCache() {
        COMPONENT_CACHE.clear();
    }

    public static void shutdown() {
        COMPONENT_CACHE.shutdown();
    }
}