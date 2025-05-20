package net.exylia.commons.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core manager for the Exylia Scoreboard system.
 * Controls all scoreboard templates and player scoreboard instances.
 */
public class ExyliaScoreboardManager {

    private final Plugin plugin;
    private final Map<String, ScoreboardTemplate> templates;
    private final Map<UUID, PlayerScoreboard> playerScoreboards;
    private final boolean placeholderAPIEnabled;

    /**
     * Creates a new ScoreboardManager.
     *
     * @param plugin The plugin instance
     */
    public ExyliaScoreboardManager(Plugin plugin) {
        this.plugin = plugin;
        this.templates = new HashMap<>();
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.placeholderAPIEnabled = ScoreboardUtil.isPlaceholderAPIEnabled();

        if (placeholderAPIEnabled) {
            plugin.getLogger().info("PlaceholderAPI found, enabling placeholder support for scoreboards.");
        }
    }

    /**
     * Creates a new scoreboard template with the given ID.
     *
     * @param templateId The unique ID for this template
     * @return A new ScoreboardTemplateBuilder instance
     */
    public ScoreboardTemplateBuilder createTemplate(String templateId) {
        return new ScoreboardTemplateBuilder(this, templateId);
    }

    /**
     * Gets a scoreboard template by ID.
     *
     * @param templateId The template ID
     * @return The template, or null if not found
     */
    public ScoreboardTemplate getTemplate(String templateId) {
        return templates.get(templateId);
    }

    /**
     * Registers a scoreboard template.
     *
     * @param template The template to register
     */
    void registerTemplate(ScoreboardTemplate template) {
        templates.put(template.getId(), template);
    }

    /**
     * Shows a scoreboard from a template to a player.
     *
     * @param player The player
     * @param templateId The template ID
     * @return The PlayerScoreboard instance
     */
    public PlayerScoreboard showScoreboard(Player player, String templateId) {
        ScoreboardTemplate template = templates.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Scoreboard template not found: " + templateId);
        }

        // Hide any existing scoreboard
        hideScoreboard(player);

        // Create and show the new scoreboard
        PlayerScoreboard playerScoreboard = new PlayerScoreboard(plugin, player, template);
        playerScoreboards.put(player.getUniqueId(), playerScoreboard);
        playerScoreboard.show();

        return playerScoreboard;
    }

    /**
     * Gets a player's active scoreboard.
     *
     * @param player The player
     * @return The player's scoreboard, or null if none is active
     */
    public PlayerScoreboard getPlayerScoreboard(Player player) {
        return playerScoreboards.get(player.getUniqueId());
    }

    /**
     * Hides the scoreboard for a player.
     *
     * @param player The player
     */
    public void hideScoreboard(Player player) {
        PlayerScoreboard scoreboard = playerScoreboards.remove(player.getUniqueId());
        if (scoreboard != null) {
            scoreboard.destroy();
        }
    }

    /**
     * Checks if a player has an active scoreboard.
     *
     * @param player The player
     * @return true if the player has an active scoreboard
     */
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }

    /**
     * Updates all player scoreboards.
     */
    public void updateAllScoreboards() {
        playerScoreboards.values().forEach(PlayerScoreboard::update);
    }

    /**
     * Gets the total number of active player scoreboards.
     *
     * @return The number of active scoreboards
     */
    public int getActiveScoreboardCount() {
        return playerScoreboards.size();
    }

    /**
     * Gets the total number of registered templates.
     *
     * @return The number of templates
     */
    public int getTemplateCount() {
        return templates.size();
    }

    /**
     * Cleans up all resources when the plugin is disabled.
     */
    public void shutdown() {
        playerScoreboards.values().forEach(PlayerScoreboard::destroy);
        playerScoreboards.clear();
        templates.clear();
    }

    /**
     * Gets the plugin instance.
     *
     * @return The plugin
     */
    Plugin getPlugin() {
        return plugin;
    }

    /**
     * Checks if PlaceholderAPI is enabled.
     *
     * @return true if PlaceholderAPI is enabled
     */
    boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}