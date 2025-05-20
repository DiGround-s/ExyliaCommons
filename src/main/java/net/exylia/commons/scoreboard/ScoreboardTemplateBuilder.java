package net.exylia.commons.scoreboard;

import net.exylia.commons.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Builder for creating scoreboard templates.
 */
public class ScoreboardTemplateBuilder {

    private final ExyliaScoreboardManager manager;
    private final String templateId;
    private ContentProvider titleProvider;
    private final Map<Integer, ScoreboardTemplate.LineTemplate> lines = new HashMap<>();
    private int updateTicks = 20;

    /**
     * Creates a new ScoreboardTemplateBuilder.
     *
     * @param manager The scoreboard manager
     * @param templateId The template ID
     */
    ScoreboardTemplateBuilder(ExyliaScoreboardManager manager, String templateId) {
        this.manager = manager;
        this.templateId = templateId;
    }

    /**
     * Sets the title using a static Component.
     *
     * @param title The title component
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder title(Component title) {
        this.titleProvider = new StaticContentProvider(title);
        return this;
    }

    /**
     * Sets the title using a static String.
     *
     * @param title The title string
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder title(String title) {
        Component component = ColorUtils.translateColors(title);
        this.titleProvider = new StaticContentProvider(component);
        return this;
    }

    /**
     * Sets the title using a dynamic provider.
     *
     * @param provider The content provider
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder title(ContentProvider provider) {
        this.titleProvider = provider;
        return this;
    }

    /**
     * Sets how often the scoreboard should update.
     *
     * @param ticks Update frequency in ticks (0 for no automatic updates)
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder updateEvery(int ticks) {
        this.updateTicks = ticks;
        return this;
    }

    /**
     * Adds a line to the scoreboard using a static Component.
     *
     * @param position The line position (0-15)
     * @param content The content component
     * @param score The score value
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, Component content, int score) {
        if (position < 0 || position > 15) {
            throw new IllegalArgumentException("Line position must be between 0 and 15");
        }

        ContentProvider provider = new StaticContentProvider(content);
        lines.put(position, new ScoreboardTemplate.LineTemplate(provider, score, null));
        return this;
    }

    /**
     * Adds a line to the scoreboard using a static String.
     *
     * @param position The line position (0-15)
     * @param content The content string
     * @param score The score value
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, String content, int score) {
        if (position < 0 || position > 15) {
            throw new IllegalArgumentException("Line position must be between 0 and 15");
        }

        Component component = ColorUtils.translateColors(content);
        ContentProvider provider = new StaticContentProvider(component);
        lines.put(position, new ScoreboardTemplate.LineTemplate(provider, score, null));
        return this;
    }

    /**
     * Adds a line to the scoreboard using a dynamic provider.
     *
     * @param position The line position (0-15)
     * @param provider The content provider
     * @param score The score value
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, ContentProvider provider, int score) {
        if (position < 0 || position > 15) {
            throw new IllegalArgumentException("Line position must be between 0 and 15");
        }

        lines.put(position, new ScoreboardTemplate.LineTemplate(provider, score, null));
        return this;
    }

    /**
     * Adds a line to the scoreboard with a custom processor.
     *
     * @param position The line position (0-15)
     * @param provider The content provider
     * @param score The score value
     * @param processor Function to process the line content
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, ContentProvider provider, int score, Function<String, String> processor) {
        if (position < 0 || position > 15) {
            throw new IllegalArgumentException("Line position must be between 0 and 15");
        }

        lines.put(position, new ScoreboardTemplate.LineTemplate(provider, score, processor));
        return this;
    }

    /**
     * Adds a line to the scoreboard with automatic score calculation (15 - position).
     *
     * @param position The line position (0-15)
     * @param content The content component
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, Component content) {
        return line(position, content, 15 - position);
    }

    /**
     * Adds a line to the scoreboard with automatic score calculation (15 - position).
     *
     * @param position The line position (0-15)
     * @param content The content string
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, String content) {
        return line(position, content, 15 - position);
    }

    /**
     * Adds a line to the scoreboard with automatic score calculation (15 - position).
     *
     * @param position The line position (0-15)
     * @param provider The content provider
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder line(int position, ContentProvider provider) {
        return line(position, provider, 15 - position);
    }

    /**
     * Adds multiple lines to the scoreboard with automatic positioning and scoring.
     *
     * @param contents The line contents
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder lines(String... contents) {
        for (int i = 0; i < contents.length; i++) {
            line(i, contents[i], contents.length - i);
        }
        return this;
    }

    /**
     * Adds multiple lines to the scoreboard with automatic positioning and scoring.
     *
     * @param contents The line contents
     * @return This builder instance
     */
    public ScoreboardTemplateBuilder lines(Component... contents) {
        for (int i = 0; i < contents.length; i++) {
            line(i, contents[i], contents.length - i);
        }
        return this;
    }

    /**
     * Builds and registers the scoreboard template.
     *
     * @return The created ScoreboardTemplate
     */
    public ScoreboardTemplate build() {
        if (titleProvider == null) {
            throw new IllegalStateException("Scoreboard title is required");
        }

        ScoreboardTemplate template = new ScoreboardTemplate(templateId, titleProvider, lines, updateTicks);
        manager.registerTemplate(template);
        return template;
    }
}