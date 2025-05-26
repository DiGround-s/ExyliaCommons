package net.exylia.commons.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static net.exylia.commons.ExyliaPlugin.isPlaceholderAPIEnabled;

/**
 * Utility class for scoreboard operations.
 */
public final class ScoreboardUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ScoreboardUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sets placeholders in a string using PlaceholderAPI.
     *
     * @param player The player
     * @param text The text with placeholders
     * @return The text with placeholders replaced
     */
    public static String setPlaceholders(Player player, String text) {
        if (!isPlaceholderAPIEnabled() || text == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * Serializes a Component to a string.
     *
     * @param component The component
     * @return The serialized string
     */
    public static String serializeComponent(Component component) {
        if (component == null) return "";
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Deserializes a string to a Component.
     *
     * @param text The text to deserialize
     * @return The component
     */
    public static Component deserializeComponent(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Validates a line position.
     *
     * @param position The line position
     * @throws IllegalArgumentException If the position is invalid
     */
    public static void validateLinePosition(int position) {
        if (position < 0 || position > 15) {
            throw new IllegalArgumentException("Line position must be between 0 and 15");
        }
    }

    /**
     * Gets a line score based on position (15 - position).
     *
     * @param position The line position
     * @return The score value
     */
    public static int getDefaultScoreForPosition(int position) {
        validateLinePosition(position);
        return 15 - position;
    }

    /**
     * Processes a component for a player, handling placeholders.
     *
     * @param player The player
     * @param component The component
     * @return The processed component
     */
    public static Component processComponent(Player player, Component component) {
        if (!isPlaceholderAPIEnabled() || component == null) return component;

        // Convert to string, process placeholders, then convert back
        String raw = serializeComponent(component);
        String processed = setPlaceholders(player, raw);

        // If no changes were made, return the original component
        if (raw.equals(processed)) {
            return component;
        }

        // Otherwise, parse the processed text back to a component
        return deserializeComponent(processed);
    }

    /**
     * Safely creates a unique entry name for scoreboard lines.
     *
     * @param linePosition The line position
     * @return A unique entry name
     */
    public static String createUniqueEntryName(int linePosition) {
        char[] colors = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        return "§" + colors[linePosition % colors.length] + "§r";
    }
}