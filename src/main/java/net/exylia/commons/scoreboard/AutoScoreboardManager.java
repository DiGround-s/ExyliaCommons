package net.exylia.commons.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Manager for automatically applying scoreboards to players when they join.
 */
public class AutoScoreboardManager implements Listener {

    private final ExyliaScoreboardManager scoreboardManager;
    private final Plugin plugin;
    private final Map<String, Function<Player, Boolean>> conditions;
    private boolean registered = false;

    /**
     * Creates a new AutoScoreboardManager.
     *
     * @param scoreboardManager The scoreboard manager
     * @param plugin The plugin instance
     */
    public AutoScoreboardManager(ExyliaScoreboardManager scoreboardManager, Plugin plugin) {
        this.scoreboardManager = scoreboardManager;
        this.plugin = plugin;
        this.conditions = new HashMap<>();
    }

    /**
     * Registers a template to be automatically applied when players join.
     *
     * @param templateId The template ID
     */
    public void registerTemplate(String templateId) {
        registerTemplate(templateId, player -> true);
    }

    /**
     * Registers a template to be automatically applied when players join,
     * if they meet a certain condition.
     *
     * @param templateId The template ID
     * @param condition The condition function
     */
    public void registerTemplate(String templateId, Function<Player, Boolean> condition) {
        conditions.put(templateId, condition);

        // Register events if not already registered
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            registered = true;

            // Apply to all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkAndApply(player);
            }
        }
    }

    /**
     * Unregisters a template from auto-applying.
     *
     * @param templateId The template ID
     */
    public void unregisterTemplate(String templateId) {
        conditions.remove(templateId);

        // Unregister events if no templates are registered
        if (conditions.isEmpty() && registered) {
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    /**
     * Unregisters all templates and stops listening for events.
     */
    public void unregisterAll() {
        conditions.clear();

        if (registered) {
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    /**
     * Checks if any template should be applied to a player, and applies it.
     *
     * @param player The player
     */
    private void checkAndApply(Player player) {
        for (Map.Entry<String, Function<Player, Boolean>> entry : conditions.entrySet()) {
            String templateId = entry.getKey();
            Function<Player, Boolean> condition = entry.getValue();

            try {
                if (condition.apply(player)) {
                    // Only show if they don't already have a scoreboard
                    if (!scoreboardManager.hasScoreboard(player)) {
                        scoreboardManager.showScoreboard(player, templateId);
                    }
                    return; // Stop after first match
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking condition for template " + templateId + ": " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Schedule a task to check conditions after a short delay
        // This allows other plugins to set up data the conditions might need
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                checkAndApply(event.getPlayer());
            }
        }, 5L); // 5 tick delay (1/4 second)
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player's scoreboard
        scoreboardManager.hideScoreboard(event.getPlayer());
    }
}