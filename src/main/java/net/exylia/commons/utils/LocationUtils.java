package net.exylia.commons.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {

    private static final String SEPARATOR = "|";

    public static Location getLocationFromString(String locationString) {
        if (locationString == null || locationString.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = locationString.split("\\" + SEPARATOR);

            if (parts.length != 6) {
                throw new IllegalArgumentException("El formato debe ser: world|x|y|z|pitch|yaw");
            }

            // Obtener el mundo
            String worldName = parts[0].trim();
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                throw new IllegalArgumentException("El mundo '" + worldName + "' no existe");
            }

            // Parsear coordenadas
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float pitch = Float.parseFloat(parts[4].trim());
            float yaw = Float.parseFloat(parts[5].trim());

            return new Location(world, x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            System.err.println("Error al parsear números en la ubicación: " + locationString);
            return null;
        } catch (Exception e) {
            System.err.println("Error al parsear la ubicación '" + locationString + "': " + e.getMessage());
            return null;
        }
    }

    public static String getStringFromLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return String.format("%s%s%.1f%s%.1f%s%.1f%s%.1f%s%.1f",
                location.getWorld().getName(),
                SEPARATOR,
                location.getX(),
                SEPARATOR,
                location.getY(),
                SEPARATOR,
                location.getZ(),
                SEPARATOR,
                location.getPitch(),
                SEPARATOR,
                location.getYaw()
        );
    }

    public static String getStringFromLocation(Location location, int decimals) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        String format = String.format("%s%s%%.%df%s%%.%df%s%%.%df%s%%.%df%s%%.%df",
                location.getWorld().getName(),
                SEPARATOR, decimals,
                SEPARATOR, decimals,
                SEPARATOR, decimals,
                SEPARATOR, decimals,
                SEPARATOR, decimals
        );

        return String.format(format,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw()
        );
    }

    public static boolean isValidLocationString(String locationString) {
        if (locationString == null || locationString.trim().isEmpty()) {
            return false;
        }

        String[] parts = locationString.split("\\" + SEPARATOR);

        if (parts.length != 6) {
            return false;
        }

        try {
            World world = Bukkit.getWorld(parts[0].trim());
            if (world == null) {
                return false;
            }

            Double.parseDouble(parts[1].trim()); // x
            Double.parseDouble(parts[2].trim()); // y
            Double.parseDouble(parts[3].trim()); // z
            Float.parseFloat(parts[4].trim());   // pitch
            Float.parseFloat(parts[5].trim());   // yaw

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Location getLocationWithoutRotation(String locationString) {
        Location loc = getLocationFromString(locationString);
        if (loc != null) {
            loc.setPitch(0);
            loc.setYaw(0);
        }
        return loc;
    }

    public static String getCoordinatesString(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return String.format("%s%s%.1f%s%.1f%s%.1f",
                location.getWorld().getName(),
                SEPARATOR,
                location.getX(),
                SEPARATOR,
                location.getY(),
                SEPARATOR,
                location.getZ()
        );
    }
}