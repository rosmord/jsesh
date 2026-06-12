package org.qenherkhopeshef.swingUtils.lists;

import java.util.Optional;

import javax.swing.JComboBox;

/**
 * Utility functions for comboboxes.
 */
public class ComboboxUtils {
    private ComboboxUtils() {
    }

    /**
     * Type safe variant of getSelectedItem().
     */

    public static <T> Optional<T> getSelectedItem(JComboBox<T> box) {
        int index = box.getSelectedIndex();
        if (index < 0) {
            return Optional.empty();
        } else {
            return Optional.of(
                    box.getItemAt(index));
        }
    }
}
