package net.exylia.commons.command.annotation;

/**
 * Información de un subcomando extraída de las anotaciones
 */
public record SubCommandInfo(String name, String usage, String permission, boolean playerOnly, String[] aliases,
                             int order) {

    /**
     * Verifica si el nombre o algún alias coincide
     */
    public boolean matches(String input) {
        if (name.equalsIgnoreCase(input)) {
            return true;
        }

        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(input)) {
                return true;
            }
        }

        return false;
    }
}