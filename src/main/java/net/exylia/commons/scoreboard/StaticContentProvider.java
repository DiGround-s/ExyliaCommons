package net.exylia.commons.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static net.exylia.commons.ExyliaPlugin.isPlaceholderAPIEnabled;

/**
 * A content provider that always returns the same component.
 */
public class StaticContentProvider implements ContentProvider {

    private final Component content;

    /**
     * Creates a new StaticContentProvider.
     *
     * @param content The static content
     */
    public StaticContentProvider(Component content) {
        this.content = content;
    }

    @Override
    public Component getContent(Player player) {
        if (isPlaceholderAPIEnabled()) {
            // Process potential placeholders even for static content
            String serialized = ScoreboardUtil.serializeComponent(content);
            String processed = ScoreboardUtil.setPlaceholders(player, serialized);

            if (!serialized.equals(processed)) {
                return ScoreboardUtil.deserializeComponent(processed);
            }
        }
        return content;
    }
}