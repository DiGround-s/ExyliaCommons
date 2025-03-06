package net.exylia.exyliaCommons.utils;


import net.exylia.exyliaCommons.ExyliaCommons;

public class DebugUtils {
    // Colores ANSI estándar
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m"; // Magenta
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Colores ANSI brillantes
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m"; // Magenta brillante
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Fondos de color ANSI
    public static final String BACKGROUND_BLACK = "\u001B[40m";
    public static final String BACKGROUND_RED = "\u001B[41m";
    public static final String BACKGROUND_GREEN = "\u001B[42m";
    public static final String BACKGROUND_YELLOW = "\u001B[43m";
    public static final String BACKGROUND_BLUE = "\u001B[44m";
    public static final String BACKGROUND_PURPLE = "\u001B[45m"; // Fondo magenta
    public static final String BACKGROUND_CYAN = "\u001B[46m";
    public static final String BACKGROUND_WHITE = "\u001B[47m";

    // Fondos brillantes
    public static final String BACKGROUND_BRIGHT_BLACK = "\u001B[100m";
    public static final String BACKGROUND_BRIGHT_RED = "\u001B[101m";
    public static final String BACKGROUND_BRIGHT_GREEN = "\u001B[102m";
    public static final String BACKGROUND_BRIGHT_YELLOW = "\u001B[103m";
    public static final String BACKGROUND_BRIGHT_BLUE = "\u001B[104m";
    public static final String BACKGROUND_BRIGHT_PURPLE = "\u001B[105m"; // Fondo magenta brillante
    public static final String BACKGROUND_BRIGHT_CYAN = "\u001B[106m";
    public static final String BACKGROUND_BRIGHT_WHITE = "\u001B[107m";

    // Otros efectos ANSI
    public static final String RESET = "\u001B[0m"; // Reset de colores y estilos
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String REVERSED = "\u001B[7m"; // Invertir colores de fondo y texto


    public static void logDebug(String message){
        if (!ExyliaCommons.getInstance().getConfigManager().getConfig("config").getBoolean("debug")) return;
        ExyliaCommons.getInstance().getLogger().info(BLACK + BACKGROUND_WHITE + "[DEBUG] " + message);
    }
    public static void logError(String message){
        ExyliaCommons.getInstance().getLogger().severe(BRIGHT_RED + "[ERROR] " + message);
    }
    public static void logWarn(String message){
        ExyliaCommons.getInstance().getLogger().warning(BRIGHT_YELLOW + "[WARN] " + message);
    }
    public static void logInfo(String message){
        ExyliaCommons.getInstance().getLogger().info(BRIGHT_CYAN + "[INFO] " + message);
    }

    public static void sendMOTD() {
        logInfo(PURPLE + " _____            _ _        " + BRIGHT_PURPLE + " _____                _        _  " + RESET);
        logInfo(PURPLE + "|  ____|          | (_)      " + BRIGHT_PURPLE + "/ ____|              | |      | | " + RESET);
        logInfo(PURPLE + "| |__  __  ___   _| |_  __ _ " + BRIGHT_PURPLE + "| |     _ __ _   _ ___| |_ __ _| | " + RESET);
        logInfo(PURPLE + "|  __| \\ \\/ / | | | | |/ _` |" + BRIGHT_PURPLE + "| |    | '__| | | / __| __/ _` | | " + RESET);
        logInfo(PURPLE + "| |____ >  <| |_| | | | (_| |" + BRIGHT_PURPLE + "| |____| |  | |_| \\__ \\ || (_| | | " + RESET);
        logInfo(PURPLE + "|______/_/\\_\\\\__, |_|_|\\__,_|" + BRIGHT_PURPLE + "\\_____|_|   \\__, |___/\\__\\__,_|_| " + RESET);
        logInfo(PURPLE + "             __/ |                      " + BRIGHT_PURPLE + " __/ |                      " + RESET);
        logInfo(PURPLE + "            |___/                       " + BRIGHT_PURPLE + "|___/                       " + RESET);
    }
}
