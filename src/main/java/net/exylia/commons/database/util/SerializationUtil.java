package net.exylia.commons.database.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for serializing and deserializing objects to and from Base64 strings.
 */
public class SerializationUtil {
    private static final Logger LOGGER = Logger.getLogger(SerializationUtil.class.getName());

    /**
     * Private constructor to prevent instantiation.
     */
    private SerializationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Serializes an ItemStack array to a Base64 string.
     *
     * @param items The ItemStack array to serialize
     * @return The Base64 encoded string
     */
    public static String itemStackArrayToBase64(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the length of the array
            dataOutput.writeInt(items.length);

            // Write each item in the array
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Convert to Base64 and return
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing ItemStack array to Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deserializes an ItemStack array from a Base64 string.
     *
     * @param base64 The Base64 encoded string
     * @return The ItemStack array
     */
    public static ItemStack[] base64ToItemStackArray(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new ItemStack[0];
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            // Read the length of the array
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read each item in the array
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            return items;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing ItemStack array from Base64: " + e.getMessage(), e);
            return new ItemStack[0];
        }
    }

    /**
     * Serializes a single ItemStack to a Base64 string.
     *
     * @param item The ItemStack to serialize
     * @return The Base64 encoded string
     */
    public static String itemStackToBase64(ItemStack item) {
        if (item == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the item
            dataOutput.writeObject(item);

            // Convert to Base64 and return
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing ItemStack to Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deserializes a single ItemStack from a Base64 string.
     *
     * @param base64 The Base64 encoded string
     * @return The ItemStack
     */
    public static ItemStack base64ToItemStack(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            // Read and return the item
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing ItemStack from Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Serializes an object to a Base64 string.
     *
     * @param object The object to serialize
     * @return The Base64 encoded string
     */
    public static String objectToBase64(Object object) {
        if (object == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the object
            dataOutput.writeObject(object);

            // Convert to Base64 and return
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing object to Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deserializes an object from a Base64 string.
     *
     * @param base64 The Base64 encoded string
     * @param <T> The expected type of the object
     * @return The deserialized object
     */
    @SuppressWarnings("unchecked")
    public static <T> T base64ToObject(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            // Read and return the object
            return (T) dataInput.readObject();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing object from Base64: " + e.getMessage(), e);
            return null;
        }
    }
}
