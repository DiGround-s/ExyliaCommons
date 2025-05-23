package net.exylia.commons.command.types;

import net.exylia.commons.command.annotation.CommandInfo;
import net.exylia.commons.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class SimpleCommand extends PermissionCommand {

    private CommandInfo commandInfo;

    public SimpleCommand(JavaPlugin plugin, String name, String permission, boolean playerOnly) {
        super(plugin, name, permission, playerOnly);
        loadCommandInfo();
    }

    public SimpleCommand(JavaPlugin plugin, String name, List<String> aliases, String permission, boolean playerOnly) {
        super(plugin, name, aliases, permission, playerOnly);
        loadCommandInfo();
    }

    private void loadCommandInfo() {
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(CommandInfo.class)) {
            commandInfo = clazz.getAnnotation(CommandInfo.class);
        }
    }

    @Override
    protected final boolean onCommand(CommandSender sender, String label, String[] args) {
        // Verificar si se pide ayuda
        if (args.length > 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) {
            showHelp(sender, label);
            return true;
        }

        // Ejecutar el comando
        return executeCommand(sender, label, args);
    }

    protected abstract boolean executeCommand(CommandSender sender, String label, String[] args);

    protected final void showHelp(CommandSender sender, String label) {
        if (commandInfo != null) {
            sender.sendMessage("");
            sender.sendMessage(ColorUtils.parse("<#8a51c4>" + plugin.getName() + " &8&l•&r <#aa76de>ᴀʏᴜᴅᴀ ᴅᴇ ᴄᴏᴍᴀɴᴅᴏꜱ"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtils.parse("<#ffc58f><> &8&l•&r <#e7cfff>Requerido &8| <#59a4ff>[] &8&l•&r <#e7cfff>Opcional"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtils.parse("<#8fffc1>/" + label + " <#a1ffc3>" + commandInfo.usage()));
            sender.sendMessage("");
        } else {
            sender.sendMessage(ColorUtils.parse("<#a33b53>Error, contacta al desarrollador. " + label));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return tabComplete(sender, args);
    }

    protected abstract List<String> tabComplete(CommandSender sender, String[] args);

    protected CommandInfo getCommandInfo() {
        return commandInfo;
    }
}