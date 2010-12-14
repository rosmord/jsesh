/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package org.qenherkhopeshef.qpreference;

import javax.swing.JComponent;

/**
 * A preference field.
 * @author rosmord
 */
public interface QPreference {
    /**
     * Returns a "group" name, used to display the prefererence.
     * May be the empty string.
     * @return
     */
    String getGroup();
    String getName(String parentName);
    /**
     * An ordering priority.
     * @return
     */
    String getPriority();
    void save();
    void load();
    JComponent getEditor();
    
}
