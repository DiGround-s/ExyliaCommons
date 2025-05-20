package net.exylia.commons.scoreboard;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * Functional content provider for dynamic content generation.
 * Uses lambda expressions for generating content.
 */
public class DynamicContentProvider implements ContentProvider {

    private final Function<Player, Component> contentFunction;

    /**
     * Creates a new DynamicContentProvider.
     *
     * @param contentFunction The function to generate content
     */
    public DynamicContentProvider(Function<Player, Component> contentFunction) {
        this.contentFunction = contentFunction;
    }

    /**
     * Creates a new DynamicContentProvider from a string function.
     *
     * @param stringFunction The function to generate string content
     * @return A new DynamicContentProvider
     */
    public static DynamicContentProvider fromString(Function<Player, String> stringFunction) {
        return new DynamicContentProvider(player -> {
            String content = stringFunction.apply(player);
            return ColorUtils.translateColors(content);
        });
    }

    /**
     * Creates a new DynamicContentProvider that uses a placeholder string.
     *
     * @param placeholderText The text with placeholders
     * @return A new DynamicContentProvider
     */
    public static DynamicContentProvider fromPlaceholder(String placeholderText) {
        return new DynamicContentProvider(player -> {
            String processed = ScoreboardUtil.setPlaceholders(player, placeholderText);
            return ColorUtils.translateColors(processed);
        });
    }

    @Override
    public Component getContent(Player player) {
        try {
            return contentFunction.apply(player);
        } catch (Exception e) {
            // Fall back to empty component if an error occurs
            return Component.empty();
        }
    }
}