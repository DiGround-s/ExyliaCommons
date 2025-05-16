package net.exylia.commons;

public class ExyliaCommons {
    private static final String VERSION = "1.0.0";

    public static String getVersion() {
        return VERSION;
    }

    public static void logInfo() {
        net.exylia.commons.utils.DebugUtils.log("<gradient:#aa76de:#8a51c4:#aa76de>ExyliaCommons v" + VERSION + "</gradient> <#a89ab5>has been loaded.");
    }
}