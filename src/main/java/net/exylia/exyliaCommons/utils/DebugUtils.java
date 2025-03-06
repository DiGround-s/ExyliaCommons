package net.exylia.exyliaCommons.utils;


import org.bukkit.plugin.java.JavaPlugin;

public class DebugUtils {
    private final JavaPlugin plugin;

    public DebugUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Colores ANSI estándar
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Colores ANSI brillantes
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Fondos de color ANSI
    public static final String BACKGROUND_BLACK = "\u001B[40m";
    public static final String BACKGROUND_RED = "\u001B[41m";
    public static final String BACKGROUND_GREEN = "\u001B[42m";
    public static final String BACKGROUND_YELLOW = "\u001B[43m";
    public static final String BACKGROUND_BLUE = "\u001B[44m";
    public static final String BACKGROUND_PURPLE = "\u001B[45m";
    public static final String BACKGROUND_CYAN = "\u001B[46m";
    public static final String BACKGROUND_WHITE = "\u001B[47m";

    // Fondos brillantes
    public static final String BACKGROUND_BRIGHT_BLACK = "\u001B[100m";
    public static final String BACKGROUND_BRIGHT_RED = "\u001B[101m";
    public static final String BACKGROUND_BRIGHT_GREEN = "\u001B[102m";
    public static final String BACKGROUND_BRIGHT_YELLOW = "\u001B[103m";
    public static final String BACKGROUND_BRIGHT_BLUE = "\u001B[104m";
    public static final String BACKGROUND_BRIGHT_PURPLE = "\u001B[105m";
    public static final String BACKGROUND_BRIGHT_CYAN = "\u001B[106m";
    public static final String BACKGROUND_BRIGHT_WHITE = "\u001B[107m";

    // Otros efectos ANSI
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String REVERSED = "\u001B[7m";


    public void logDebug(Boolean enabled, String message){
        if (!enabled) return;
        plugin.getLogger().info(BLACK + BACKGROUND_WHITE + "[DEBUG] " + message);
    }
    public void logError(String message){
        plugin.getLogger().severe(BRIGHT_RED + "[ERROR] " + message);
    }
    public void logWarn(String message){
        plugin.getLogger().warning(BRIGHT_YELLOW + "[WARN] " + message);
    }
    public void logInfo(String message){
        plugin.getLogger().info(BRIGHT_CYAN + "[INFO] " + message);
    }
}
