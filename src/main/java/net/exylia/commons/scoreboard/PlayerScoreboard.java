package net.exylia.commons.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active scoreboard instance for a player.
 * This applies a ScoreboardTemplate to a specific player.
 */
public class PlayerScoreboard {

    private final Plugin plugin;
    private final Player player;
    private final ScoreboardTemplate template;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private int taskId = -1;
    private boolean visible = false;

    private static final ScoreboardManager SCOREBOARD_MANAGER = Bukkit.getScoreboardManager();
    private static final String OBJECTIVE_NAME = "exylia";

    /**
     * Creates a new PlayerScoreboard.
     *
     * @param plugin The plugin instance
     * @param player The player
     * @param template The scoreboard template
     */
    PlayerScoreboard(Plugin plugin, Player player, ScoreboardTemplate template) {
        this.plugin = plugin;
        this.player = player;
        this.template = template;

        // Create the scoreboard
        this.scoreboard = SCOREBOARD_MANAGER.getNewScoreboard();
        Component title = template.getTitle(player);
        this.objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, title);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Start the update task if needed
        if (template.getUpdateTicks() > 0) {
            startUpdateTask();
        }
    }

    /**
     * Shows the scoreboard to the player.
     *
     * @return This instance for chaining
     */
    public PlayerScoreboard show() {
        if (visible) return this;

        visible = true;
        update();
        player.setScoreboard(scoreboard);

        if (taskId == -1 && template.getUpdateTicks() > 0) {
            startUpdateTask();
        }

        return this;
    }

    /**
     * Hides the scoreboard from the player.
     *
     * @return This instance for chaining
     */
    public PlayerScoreboard hide() {
        if (!visible) return this;

        visible = false;
        player.setScoreboard(SCOREBOARD_MANAGER.getMainScoreboard());

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        return this;
    }

    /**
     * Toggles the visibility of the scoreboard.
     *
     * @return This instance for chaining
     */
    public PlayerScoreboard toggle() {
        return visible ? hide() : show();
    }

    /**
     * Updates the scoreboard content.
     *
     * @return This instance for chaining
     */
    public PlayerScoreboard update() {
        if (!visible) return this;
        if (!player.isOnline()) {
            destroy();
            return this;
        }

        try {
            // Update the title
            objective.displayName(template.getTitle(player));

            // Clear existing scores
            for (String entry : new HashSet<>(scoreboard.getEntries())) {
                scoreboard.resetScores(entry);
            }

            // Update all lines
            Map<Integer, ScoreboardTemplate.LineTemplate> lines = template.getLines();
            for (Map.Entry<Integer, ScoreboardTemplate.LineTemplate> entry : lines.entrySet()) {
                int position = entry.getKey();
                ScoreboardTemplate.LineTemplate lineTemplate = entry.getValue();
                Component content = lineTemplate.getContent(player);

                // Use a team to set the line content
                String entryName = getUniqueEntryName(position);
                Team team = scoreboard.getTeam("line" + position);
                if (team == null) {
                    team = scoreboard.registerNewTeam("line" + position);
                }

                team.prefix(content);
                team.addEntry(entryName);
                objective.getScore(entryName).setScore(lineTemplate.getScore());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating scoreboard for player " + player.getName() + ": " + e.getMessage());
        }

        return this;
    }

    /**
     * Destroys this scoreboard, cleaning up resources.
     */
    public void destroy() {
        hide();

        // Clean up objective and teams
        try {
            objective.unregister();
            for (Team team : scoreboard.getTeams()) {
                team.unregister();
            }
        } catch (Exception ignored) {
            // Ignore exceptions during cleanup
        }
    }

    /**
     * Gets the player this scoreboard belongs to.
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player this scoreboard belongs to.
     *
     * @return The player UUID
     */
    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    /**
     * Gets the template this scoreboard is based on.
     *
     * @return The template
     */
    public ScoreboardTemplate getTemplate() {
        return template;
    }

    /**
     * Checks if the scoreboard is visible.
     *
     * @return true if visible, false if hidden
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets a unique entry name for a line.
     *
     * @param line The line position
     * @return A unique entry name
     */
    private String getUniqueEntryName(int line) {
        // Use color codes to create unique entries for each line
        char[] colors = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        return "§" + colors[line % colors.length] + "§r";
    }

    /**
     * Starts the update task.
     */
    private void startUpdateTask() {
        int updateTicks = template.getUpdateTicks();
        if (updateTicks <= 0) return;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::update, updateTicks, updateTicks);
    }
}