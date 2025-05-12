package net.exylia.commons.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Utilidades optimizadas para manejar colores y componentes de texto
 * con soporte para operaciones asíncronas
 */
public class ColorUtils {

    private static final Map<Character, String> COLOR_MAP;

    // Cache para componentes comunes
    private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 500;

    static {
        Map<Character, String> map = new HashMap<>();
        map.put('0', "<black>");
        map.put('1', "<dark_blue>");
        map.put('2', "<dark_green>");
        map.put('3', "<dark_aqua>");
        map.put('4', "<dark_red>");
        map.put('5', "<dark_purple>");
        map.put('6', "<gold>");
        map.put('7', "<gray>");
        map.put('8', "<dark_gray>");
        map.put('9', "<blue>");
        map.put('a', "<green>");
        map.put('b', "<aqua>");
        map.put('c', "<red>");
        map.put('d', "<light_purple>");
        map.put('e', "<yellow>");
        map.put('f', "<white>");
        map.put('k', "<obfuscated>");
        map.put('l', "<bold>");
        map.put('m', "<strikethrough>");
        map.put('n', "<underline>");
        map.put('o', "<italic>");
        map.put('r', "<reset>");
        COLOR_MAP = Map.copyOf(map);
    }

    /**
     * Traduce códigos de color a componentes Adventure
     * @param message Mensaje con códigos de color
     * @return Componente con colores y formato aplicados
     */
    public static Component translateColors(String message) {
        if (message == null) {
            return Component.empty();
        }

        // Verificar caché primero
        Component cached = COMPONENT_CACHE.get(message);
        if (cached != null) {
            return cached;
        }

        // Si no tiene códigos de color para procesar
        if (!message.contains("&")) {
            Component component = MiniMessage.miniMessage().deserialize(message)
                    .decoration(TextDecoration.ITALIC, false);

            // Guardar en caché si no es demasiado grande
            if (message.length() <= 100) {
                cacheComponent(message, component);
            }

            return component;
        }

        StringBuilder builder = new StringBuilder(message.length() + 16);
        int length = message.length();

        for (int i = 0; i < length; i++) {
            char c = message.charAt(i);
            if (c == '&' && i + 1 < length) {
                char next = message.charAt(i + 1);
                String replacement = COLOR_MAP.get(next);
                if (replacement != null) {
                    builder.append(replacement);
                    i++;
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
            }
        }

        Component result = MiniMessage.miniMessage().deserialize(builder.toString())
                .decoration(TextDecoration.ITALIC, false);

        // Guardar en caché si no es demasiado grande
        if (message.length() <= 100) {
            cacheComponent(message, result);
        }

        return result;
    }

    /**
     * Traduce códigos de color asíncronamente
     * @param message Mensaje con códigos de color
     * @return CompletableFuture con el componente procesado
     */
    public static CompletableFuture<Component> translateColorsAsync(String message) {
        return CompletableFuture.supplyAsync(() -> translateColors(message));
    }

    /**
     * Traduce una lista de mensajes a componentes
     * @param messages Lista de mensajes con códigos de color
     * @return Lista de componentes procesados
     */
    public static List<Component> translateColors(List<String> messages) {
        return messages.stream()
                .map(ColorUtils::translateColors)
                .collect(Collectors.toList());
    }

    /**
     * Traduce una lista de mensajes asíncronamente
     * @param messages Lista de mensajes con códigos de color
     * @return CompletableFuture con la lista de componentes
     */
    public static CompletableFuture<List<Component>> translateColorsAsync(List<String> messages) {
        return CompletableFuture.supplyAsync(() -> translateColors(messages));
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
        if (input.startsWith("&") && input.length() == 2) {
            String replacement = COLOR_MAP.get(input.charAt(1));
            if (replacement != null) {
                return replacement;
            }
        }

        DebugUtils.logWarn("No se pudo normalizar el color: " + input + ". Usando color blanco por defecto.");
        return "<#ffffff>";
    }

    /**
     * Traduce códigos de color usando el sistema antiguo de ChatColor
     * @param message Mensaje con códigos de color
     * @return String con colores aplicados en formato legacy
     */
    public static String oldTranslateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', GradientUtils.applyGradientsAndHex(message));
    }

    /**
     * Envía un mensaje con colores a un jugador
     * @param player Jugador destinatario
     * @param message Mensaje con códigos de color
     */
    public static void sendPlayerMessage(Player player, String message) {
        Component component = translateColors(message);
        player.sendMessage(component);
    }

    /**
     * Envía un mensaje con colores a un jugador asíncronamente
     * @param player Jugador destinatario
     * @param message Mensaje con códigos de color
     * @return CompletableFuture que completa cuando se envía el mensaje
     */
    public static CompletableFuture<Void> sendPlayerMessageAsync(Player player, String message) {
        return translateColorsAsync(message)
                .thenAccept(player::sendMessage);
    }

    /**
     * Envía un mensaje con colores a un CommandSender
     * @param sender Destinatario del mensaje
     * @param message Mensaje con códigos de color
     */
    public static void sendSenderMessage(CommandSender sender, String message) {
        Component component = translateColors(message);
        sender.sendMessage(component);
    }

    /**
     * Envía un mensaje con colores a un CommandSender asíncronamente
     * @param sender Destinatario del mensaje
     * @param message Mensaje con códigos de color
     * @return CompletableFuture que completa cuando se envía el mensaje
     */
    public static CompletableFuture<Void> sendSenderMessageAsync(CommandSender sender, String message) {
        return translateColorsAsync(message)
                .thenAccept(sender::sendMessage);
    }

    /**
     * Envía un componente a un jugador
     * @param player Jugador destinatario
     * @param component Componente a enviar
     */
    public static void sendPlayerMessage(Player player, Component component) {
        player.sendMessage(component);
    }

    /**
     * Envía un componente a un CommandSender
     * @param sender Destinatario del mensaje
     * @param component Componente a enviar
     */
    public static void sendSenderMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    /**
     * Muestra una barra de jefe a un jugador
     * @param player Jugador al que mostrar la barra
     * @param bossBar Barra de jefe a mostrar
     */
    public static void showPlayerBossBar(Player player, BossBar bossBar) {
        player.showBossBar(bossBar);
    }

    /**
     * Muestra una barra de jefe a todos los jugadores
     * @param bossBar Barra de jefe a mostrar
     */
    public static void showPlayersBossBar(BossBar bossBar) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    /**
     * Muestra una barra de jefe a todos los jugadores asíncronamente
     * @param bossBar Barra de jefe a mostrar
     * @return CompletableFuture que completa cuando la barra ha sido mostrada
     */
    public static CompletableFuture<Void> showPlayersBossBarAsync(BossBar bossBar) {
        return CompletableFuture.runAsync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showBossBar(bossBar);
            }
        });
    }

    /**
     * Oculta una barra de jefe para un jugador
     * @param player Jugador al que ocultar la barra
     * @param bossBar Barra de jefe a ocultar
     */
    public static void hidePlayerBossBar(Player player, BossBar bossBar) {
        player.hideBossBar(bossBar);
    }

    /**
     * Oculta una barra de jefe para todos los jugadores
     * @param bossBar Barra de jefe a ocultar
     */
    public static void hidePlayersBossBar(BossBar bossBar) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
    }

    /**
     * Oculta una barra de jefe para todos los jugadores asíncronamente
     * @param bossBar Barra de jefe a ocultar
     * @return CompletableFuture que completa cuando la barra ha sido ocultada
     */
    public static CompletableFuture<Void> hidePlayersBossBarAsync(BossBar bossBar) {
        return CompletableFuture.runAsync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(bossBar);
            }
        });
    }

    /**
     * Muestra un título a un jugador
     * @param player Jugador al que mostrar el título
     * @param title Título a mostrar
     */
    public static void sendPlayerTitle(Player player, Title title) {
        player.showTitle(title);
    }

    /**
     * Crea un título a partir de mensajes con códigos de color
     * @param main Texto principal del título
     * @param subtitle Subtítulo
     * @param fadeIn Tiempo de aparición (ticks)
     * @param stay Tiempo que permanece visible (ticks)
     * @param fadeOut Tiempo de desaparición (ticks)
     * @return Objeto Title configurado
     */
    public static Title createTitle(String main, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component mainComponent = translateColors(main);
        Component subtitleComponent = translateColors(subtitle);

        return Title.title(
                mainComponent,
                subtitleComponent,
                Title.Times.of(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        );
    }

    /**
     * Crea y muestra un título a un jugador en una sola operación
     * @param player Jugador destinatario
     * @param main Texto principal del título
     * @param subtitle Subtítulo
     * @param fadeIn Tiempo de aparición (ticks)
     * @param stay Tiempo que permanece visible (ticks)
     * @param fadeOut Tiempo de desaparición (ticks)
     */
    public static void sendPlayerTitle(Player player, String main, String subtitle,
                                       int fadeIn, int stay, int fadeOut) {
        Title title = createTitle(main, subtitle, fadeIn, stay, fadeOut);
        player.showTitle(title);
    }

    /**
     * Envía un mensaje con colores a todos los jugadores
     * @param message Mensaje con códigos de color
     */
    public static void sendBroadcastMessage(String message) {
        Component component = translateColors(message);
        Bukkit.broadcast(component);
    }

    /**
     * Envía un mensaje con colores a todos los jugadores asíncronamente
     * @param message Mensaje con códigos de color
     * @return CompletableFuture que completa cuando el mensaje ha sido enviado
     */
    public static CompletableFuture<Void> sendBroadcastMessageAsync(String message) {
        return translateColorsAsync(message)
                .thenAccept(Bukkit::broadcast);
    }

    /**
     * Envía un mensaje a múltiples audiencias de forma eficiente
     * @param audiences Lista de audiencias (jugadores, consola, etc.)
     * @param message Mensaje con códigos de color
     */
    public static void sendToAudiences(List<? extends Audience> audiences, String message) {
        Component component = translateColors(message);
        audiences.forEach(audience -> audience.sendMessage(component));
    }

    /**
     * Envía un mensaje a múltiples audiencias de forma eficiente y asíncrona
     * @param audiences Lista de audiencias (jugadores, consola, etc.)
     * @param message Mensaje con códigos de color
     * @return CompletableFuture que completa cuando todos los mensajes han sido enviados
     */
    public static CompletableFuture<Void> sendToAudiencesAsync(List<? extends Audience> audiences, String message) {
        return translateColorsAsync(message)
                .thenAccept(component ->
                        audiences.forEach(audience -> audience.sendMessage(component))
                );
    }

    /**
     * Guarda un componente en caché para su reutilización
     * @param key Mensaje original como clave
     * @param component Componente procesado
     */
    private static void cacheComponent(String key, Component component) {
        // Evitar desbordamiento de caché
        if (COMPONENT_CACHE.size() >= MAX_CACHE_SIZE) {
            // Simple política de eliminación: eliminar una entrada aleatoria
            if (!COMPONENT_CACHE.isEmpty()) {
                String randomKey = COMPONENT_CACHE.keySet().iterator().next();
                COMPONENT_CACHE.remove(randomKey);
            }
        }

        COMPONENT_CACHE.put(key, component);
    }

    /**
     * Limpia la caché de componentes
     */
    public static void clearCache() {
        COMPONENT_CACHE.clear();
    }
}