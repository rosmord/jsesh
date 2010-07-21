/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package org.qenherkhopeshef.qpreference;

import java.util.HashMap;

import javax.swing.JComponent;

/**
 * Build components for preferences.
 * @author rosmord
 */
public class QPreferenceEditorFactory {

    private HashMap factoryMap= new HashMap();


    /**
     * Registers a new Factory for a specific preference class.
     * @param qpreference
     * @param singleFactory
     */
    public void registerFactoryFor(Class qpreference, QPreferenceEditorClassFactory singleFactory) {

    }


    public JComponent buildEditorFor(QPreference qPreference) {
        throw new UnsupportedOperationException("TODO");
    }
}
