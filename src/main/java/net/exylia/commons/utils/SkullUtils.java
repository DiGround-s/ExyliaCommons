package net.exylia.commons.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

import static net.exylia.commons.utils.DebugUtils.logWarn;

public class SkullUtils {

    public static ItemStack createHeadFromBase64(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        if (base64 == null || base64.isEmpty()) {
            return head;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) {
            return head;
        }

        try {
            if (tryModernMethod(headMeta, base64)) {
                head.setItemMeta(headMeta);
                return head;
            }

            if (tryLegacyMethod(headMeta, base64)) {
                head.setItemMeta(headMeta);
                return head;
            }

        } catch (Exception e) {
            logWarn("Failed to create head from base64: " + e.getMessage());
        }

        head.setItemMeta(headMeta);
        return head;
    }

    private static boolean tryModernMethod(SkullMeta headMeta, String base64) {
        try {
            Class<?> resolvableProfileClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
            Class<?> gameProfileClass = GameProfile.class;

            GameProfile profile = new GameProfile(UUID.randomUUID(), "CustomHead");
            profile.getProperties().put("textures", new Property("textures", base64));

            Object resolvableProfile = resolvableProfileClass.getConstructor(gameProfileClass)
                    .newInstance(profile);

            Field profileField = findProfileField(headMeta.getClass());
            if (profileField != null) {
                profileField.setAccessible(true);
                profileField.set(headMeta, resolvableProfile);
                return true;
            }

        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean tryLegacyMethod(SkullMeta headMeta, String base64) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "CustomHead");
            profile.getProperties().put("textures", new Property("textures", base64));

            Field profileField = findProfileField(headMeta.getClass());
            if (profileField != null) {
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
                return true;
            }

        } catch (Exception ignored) {
        }
        return false;
    }

    private static Field findProfileField(Class<?> metaClass) {
        // Buscar diferentes nombres de campo según la versión
        String[] possibleFieldNames = {"profile", "serializedProfile"};

        for (String fieldName : possibleFieldNames) {
            try {
                Field field = metaClass.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        Field[] fields = metaClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().getSimpleName().contains("Profile")) {
                return field;
            }
        }

        return null;
    }

    public static ItemStack createHeadFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        String textureJson = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url);
        String base64 = Base64.getEncoder().encodeToString(textureJson.getBytes());

        return createHeadFromBase64(base64);
    }

    public static ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        if (playerName == null || playerName.isEmpty()) {
            return head;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) {
            return head;
        }

        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            headMeta.setOwningPlayer(offlinePlayer);
        } catch (Exception e) {
            logWarn("Could not set skull owner for player: " + playerName + " - " + e.getMessage());
        }

        head.setItemMeta(headMeta);
        return head;
    }

    public static String getEncodedTexture(String url) {
        String fullUrl;
        if (url.startsWith("http")) {
            fullUrl = url;
        } else {
            fullUrl = "http://textures.minecraft.net/texture/" + url;
        }

        String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", fullUrl);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    public static ItemStack createHeadFromBase64Fallback(String base64) {
        try {
            UUID textureUUID = UUID.nameUUIDFromBytes(base64.getBytes());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(textureUUID);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                head.setItemMeta(meta);
            }

            return head;
        } catch (Exception e) {
            logWarn("Fallback method also failed: " + e.getMessage());
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }
}