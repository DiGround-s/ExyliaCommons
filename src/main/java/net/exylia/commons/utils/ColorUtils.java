package net.exylia.commons.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidades para manejar colores y componentes de texto
 */
public class ColorUtils {

    private static final Map<Character, String> COLOR_MAP;

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
        if (message == null || !message.contains("&")) {
            assert message != null;
            return MiniMessage.miniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false);
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

        return MiniMessage.miniMessage().deserialize(builder.toString())
                .decoration(TextDecoration.ITALIC, false);
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
        Component component = ColorUtils.translateColors(message);
        player.sendMessage(component);
    }

    /**
     * Envía un mensaje con colores a un CommandSender
     * @param sender Destinatario del mensaje
     * @param message Mensaje con códigos de color
     */
    public static void sendSenderMessage(CommandSender sender, String message) {
        Component component = ColorUtils.translateColors(message);
        sender.sendMessage(component);
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
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showBossBar(bossBar);
        }
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
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.hideBossBar(bossBar);
        }
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
     * Envía un mensaje con colores a todos los jugadores
     * @param message Mensaje con códigos de color
     */
    public static void sendBroadcastMessage(String message) {
        Component component = ColorUtils.translateColors(message);
        Bukkit.broadcast(component);
    }
}