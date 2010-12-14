/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package org.qenherkhopeshef.qpreference;

import javax.swing.JComponent;

/**
 * Graphical editor for a specific QPreference.
 * @author rosmord
 */
public interface QPreferenceEditor {
    /**
     * Returns the component.
     * @return
     */
    JComponent getComponent();
    /**
     * Fills the component with the given value.
     * @param value
     */
    void init(QPreference value);

    /**
     * Copy the edited value back into the QPreference.
     */
    void validate();
}
