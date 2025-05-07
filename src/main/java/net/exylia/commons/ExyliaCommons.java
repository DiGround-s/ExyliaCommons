package net.exylia.commons;

/**
 * Clase principal de la biblioteca ExyliaCommons
 * Proporciona utilidades y herramientas compartidas para plugins de Exylia
 */
public class ExyliaCommons {
    private static final String VERSION = "1.0.0";

    /**
     * Obtiene la versión actual de la biblioteca
     * @return La versión de ExyliaCommons
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Muestra información sobre la biblioteca en la consola
     */
    public static void logInfo() {
        net.exylia.commons.utils.DebugUtils.log("<gradient:#aa76de:#8a51c4:#aa76de>ExyliaCommons v" + VERSION + "</gradient> <#a89ab5>cargada correctamente");
    }
}