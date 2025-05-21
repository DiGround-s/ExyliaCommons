package net.exylia.commons.scoreboard;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * A provider interface for dynamic content generation.
 * This allows for both static and dynamic content in scoreboards.
 */
@FunctionalInterface
public interface ContentProvider {

    /**
     * Gets the content for a specific player.
     *
     * @param player The player
     * @return The content component
     */
    Component getContent(Player player);

    /**
     * Creates a content provider from a lambda expression.
     *
     * @param function The function to generate content
     * @return A content provider
     */
    static ContentProvider of(java.util.function.Function<Player, Component> function) {
        return function::apply;
    }

    /**
     * Creates a content provider from a string lambda expression.
     * The string will be automatically converted to a component.
     *
     * @param function The function to generate content
     * @return A content provider
     */
    static ContentProvider ofString(java.util.function.Function<Player, String> function) {
        return player -> {
            String content = function.apply(player);
            return ColorUtils.parse(content);
        };
    }

    /**
     * Creates a content provider from a static component.
     *
     * @param component The static component
     * @return A content provider
     */
    static ContentProvider fixed(Component component) {
        return new StaticContentProvider(component);
    }

    /**
     * Creates a content provider from a static string.
     *
     * @param text The static text
     * @return A content provider
     */
    static ContentProvider fixed(String text) {
        Component component = ColorUtils.parse(text);
        return new StaticContentProvider(component);
    }

    /**
     * Creates a placeholder-based content provider.
     *
     * @param text The text with placeholders
     * @return A content provider
     */
    static ContentProvider placeholder(String text) {
        return player -> {
            String processed = ScoreboardUtil.setPlaceholders(player, text);
            return ColorUtils.parse(processed);
        };
    }
}