package net.exylia.commons.utils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
    /**
     * Convierte una cadena de slots (como "0-26") a un array de enteros
     *
     * @param slotsString La cadena a convertir
     * @param rows El número de filas del menú para validación
     * @return Array de enteros con los slots
     */
    public static int[] parseSlots(String slotsString, int rows) {
        List<Integer> slotsList = new ArrayList<>();

        String[] parts = slotsString.split(",");

        for (String part : parts) {
            part = part.trim();

            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());

                for (int i = start; i <= end; i++) {
                    if (i < rows * 9) {
                        slotsList.add(i);
                    }
                }
            }
            else {
                int slot = Integer.parseInt(part);
                if (slot < rows * 9) {
                    slotsList.add(slot);
                }
            }
        }

        int[] slots = new int[slotsList.size()];
        for (int i = 0; i < slotsList.size(); i++) {
            slots[i] = slotsList.get(i);
        }

        return slots;
    }
}
