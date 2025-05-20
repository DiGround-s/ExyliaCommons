package net.exylia.commons.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Template definition for a scoreboard that can be applied to multiple players.
 * This is the reusable configuration that defines how a scoreboard looks and behaves.
 */
public class ScoreboardTemplate {

    private final String id;
    private final Map<Integer, LineTemplate> lines;
    private final ContentProvider titleProvider;
    private final int updateTicks;

    ScoreboardTemplate(String id, ContentProvider titleProvider, Map<Integer, LineTemplate> lines, int updateTicks) {
        this.id = id;
        this.titleProvider = titleProvider;
        this.lines = new HashMap<>(lines);
        this.updateTicks = updateTicks;
    }

    /**
     * Gets the template ID.
     *
     * @return The template ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the update frequency in ticks.
     *
     * @return The update frequency
     */
    public int getUpdateTicks() {
        return updateTicks;
    }

    /**
     * Gets the title for a specific player.
     *
     * @param player The player
     * @return The title component
     */
    public Component getTitle(Player player) {
        return titleProvider.getContent(player);
    }

    /**
     * Gets all line templates.
     *
     * @return The line templates
     */
    public Map<Integer, LineTemplate> getLines() {
        return lines;
    }

    /**
     * Gets a specific line template.
     *
     * @param position The line position
     * @return The line template, or null if none exists at this position
     */
    public LineTemplate getLine(int position) {
        return lines.get(position);
    }

    /**
     * Represents a line template in the scoreboard.
     */
    public static class LineTemplate {
        private final ContentProvider contentProvider;
        private final int score;
        private final Function<String, String> processor;

        /**
         * Creates a new line template.
         *
         * @param contentProvider The content provider
         * @param score The score value
         * @param processor Optional processor function
         */
        public LineTemplate(ContentProvider contentProvider, int score, Function<String, String> processor) {
            this.contentProvider = contentProvider;
            this.score = score;
            this.processor = processor;
        }

        /**
         * Gets the content for a specific player.
         *
         * @param player The player
         * @return The content component
         */
        public Component getContent(Player player) {
            Component content = contentProvider.getContent(player);

            if (processor != null && content != null) {
                String raw = ScoreboardUtil.serializeComponent(content);
                raw = processor.apply(raw);
                return ScoreboardUtil.deserializeComponent(raw);
            }

            return content;
        }

        /**
         * Gets the score value.
         *
         * @return The score
         */
        public int getScore() {
            return score;
        }
    }
}